package com.hotelbooking.payment.application;

import com.hotelbooking.booking.application.BookingService;
import com.hotelbooking.booking.domain.Booking;
import com.hotelbooking.booking.domain.BookingId;
import com.hotelbooking.common.domain.error.ConflictException;
import com.hotelbooking.payment.application.processor.PaymentProcessor;
import com.hotelbooking.payment.application.processor.PaymentProcessors;
import com.hotelbooking.payment.domain.Payment;
import com.hotelbooking.payment.domain.PaymentRepository;
import com.hotelbooking.payment.domain.PaymentStatus;
import com.hotelbooking.payment.domain.gateway.GatewayResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

/**
 * Takes payment for a booking in three steps, so no database transaction stays open during the gateway call:
 * <ol>
 *   <li>Lock the booking, resolve the Idempotency-Key, record an INITIATED payment, commit.</li>
 *   <li>Call the gateway.</li>
 *   <li>Lock the booking again, record the result, and confirm the booking on success.</li>
 * </ol>
 * The booking lock serializes payment attempts with cancellation and hold expiry on the same booking.
 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository payments;
    private final BookingService bookings;
    private final PaymentProcessors processors;
    private final RefundService refunds;
    private final TransactionTemplate transactions;
    private final Clock clock;

    public PaymentService(PaymentRepository payments, BookingService bookings, PaymentProcessors processors,
                          RefundService refunds, PlatformTransactionManager transactionManager, Clock clock) {
        this.payments = payments;
        this.bookings = bookings;
        this.processors = processors;
        this.refunds = refunds;
        this.transactions = new TransactionTemplate(transactionManager);
        this.clock = clock;
    }

    public PaymentResult pay(PayForBookingCommand command) {
        PaymentProcessor processor = processors.forMethod(command.instrument().method());
        processor.validate(command.instrument());
        String fingerprint = RequestFingerprint.of(command.bookingId(), command.instrument());

        PaymentResult attempt = transactions.execute(status -> startAttempt(command, fingerprint));
        if (attempt.replayed()) {
            return attempt;
        }

        GatewayResult result = processor.charge(attempt.payment(), command.instrument());
        Payment payment = transactions.execute(status -> recordChargeResult(attempt.payment(), result));

        if (payment.status() == PaymentStatus.REFUND_PENDING) {
            payment = refunds.processRefund(payment.id());
        }
        return PaymentResult.created(payment);
    }

    @Transactional(readOnly = true)
    public List<Payment> findForBooking(BookingId bookingId) {
        bookings.get(bookingId);
        return payments.findByBooking(bookingId);
    }

    private PaymentResult startAttempt(PayForBookingCommand command, String fingerprint) {
        // Lock first: two requests with the same key queue here, and the second one then sees the first's payment.
        Booking booking = bookings.getForUpdate(command.bookingId());

        Optional<Payment> earlier = payments.findByIdempotencyKey(command.idempotencyKey());
        if (earlier.isPresent()) {
            return PaymentResult.replayed(verifiedReplay(earlier.get(), fingerprint));
        }

        booking.ensureAwaitingPayment(clock.instant());
        if (payments.existsByBookingAndStatus(booking.id(), PaymentStatus.INITIATED)) {
            throw paymentInProgress(booking.id());
        }
        Payment payment = Payment.initiate(booking.id(), booking.totalAmount(), command.instrument(),
                command.idempotencyKey(), fingerprint, clock.instant());
        payments.save(payment);
        log.info("Payment {} initiated for booking {}: {} by {}", payment.id(), booking.id(), payment.amount(),
                payment.method());
        return PaymentResult.created(payment);
    }

    private Payment verifiedReplay(Payment earlier, String fingerprint) {
        earlier.ensureSameRequest(fingerprint);
        if (earlier.isInFlight()) {
            throw paymentInProgress(earlier.bookingId());
        }
        log.info("Payment {} replayed for Idempotency-Key {} (status {}); no new charge",
                earlier.id(), earlier.idempotencyKey(), earlier.status());
        return earlier;
    }

    private Payment recordChargeResult(Payment attempt, GatewayResult result) {
        Booking booking = bookings.getForUpdate(attempt.bookingId());
        Payment payment = payments.findById(attempt.id()).orElseThrow();

        if (!result.successful()) {
            // The booking stays PENDING_PAYMENT, so the guest can retry with a new key until the hold expires.
            payment.markFailed(result.failureReason());
            log.info("Payment {} for booking {} failed: {}", payment.id(), booking.id(), result.failureReason());
            return payments.save(payment);
        }

        payment.markSucceeded(result.reference());
        log.info("Payment {} for booking {} succeeded, gateway reference {}", payment.id(), booking.id(),
                result.reference());
        if (booking.isAwaitingPayment()) {
            bookings.confirm(booking.id());
        } else {
            // The booking was cancelled or expired while the charge was in flight: give the money back.
            log.warn("Booking {} became {} while payment {} was at the gateway; refunding {} in full",
                    booking.id(), booking.status(), payment.id(), payment.amount());
            payment.requestRefund(payment.amount());
        }
        return payments.save(payment);
    }

    private static ConflictException paymentInProgress(BookingId bookingId) {
        return new ConflictException("PAYMENT_IN_PROGRESS",
                "A payment for booking %s is already in progress. Retry shortly.".formatted(bookingId));
    }
}
