package com.hotelbooking.payment.application;

import com.hotelbooking.booking.application.BookingService;
import com.hotelbooking.booking.domain.BookingId;
import com.hotelbooking.common.domain.Money;
import com.hotelbooking.common.domain.error.ResourceNotFoundException;
import com.hotelbooking.payment.application.processor.PaymentProcessors;
import com.hotelbooking.payment.domain.Payment;
import com.hotelbooking.payment.domain.PaymentId;
import com.hotelbooking.payment.domain.PaymentRepository;
import com.hotelbooking.payment.domain.PaymentStatus;
import com.hotelbooking.payment.domain.gateway.GatewayResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Refunds follow the same pattern as charges: record the intent in a transaction, call the gateway outside it,
 * then record the outcome. A failed refund never undoes the cancellation: the payment moves to REFUND_FAILED
 * and keeps the amount owed.
 */
@Service
public class RefundService {

    private static final Logger log = LoggerFactory.getLogger(RefundService.class);

    private final PaymentRepository payments;
    private final BookingService bookings;
    private final PaymentProcessors processors;
    private final TransactionTemplate transactions;

    public RefundService(PaymentRepository payments, BookingService bookings, PaymentProcessors processors,
                         PlatformTransactionManager transactionManager) {
        this.payments = payments;
        this.bookings = bookings;
        this.processors = processors;
        this.transactions = new TransactionTemplate(transactionManager);
    }

    /** Marks the booking's successful payment for refund, inside the caller's transaction. */
    @Transactional(propagation = Propagation.MANDATORY)
    public Payment requestRefund(BookingId bookingId, Money amount) {
        Payment payment = payments.findByBookingAndStatus(bookingId, PaymentStatus.SUCCEEDED)
                .orElseThrow(() -> new IllegalStateException("Booking %s has no successful payment".formatted(bookingId)));
        payment.requestRefund(amount);
        payments.save(payment);
        log.info("Refund of {} requested for payment {} (booking {})", amount, payment.id(), bookingId);
        return payment;
    }

    /** Sends a pending refund to the gateway. Must be called outside any transaction. */
    public Payment processRefund(PaymentId paymentId) {
        Payment pending = transactions.execute(status -> load(paymentId));
        if (pending.status() != PaymentStatus.REFUND_PENDING) {
            return pending;
        }
        GatewayResult result = processors.forMethod(pending.method()).refund(pending);
        return transactions.execute(status -> recordRefundResult(pending, result));
    }

    private Payment recordRefundResult(Payment pending, GatewayResult result) {
        bookings.getForUpdate(pending.bookingId());
        Payment payment = load(pending.id());
        if (result.successful()) {
            payment.markRefunded(result.reference());
            log.info("Payment {} refunded {}, gateway reference {}", payment.id(), payment.refundAmount(),
                    result.reference());
        } else {
            payment.markRefundFailed(result.failureReason());
            // Nothing retries a failed refund automatically, so this needs someone to act on it.
            log.error("Refund of {} for payment {} (booking {}) failed: {}. The amount is still owed to the guest.",
                    payment.refundAmount(), payment.id(), payment.bookingId(), result.failureReason());
        }
        return payments.save(payment);
    }

    private Payment load(PaymentId id) {
        return payments.findById(id).orElseThrow(() -> new ResourceNotFoundException("Payment", id));
    }
}
