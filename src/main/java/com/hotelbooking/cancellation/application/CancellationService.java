package com.hotelbooking.cancellation.application;

import com.hotelbooking.booking.application.BookingService;
import com.hotelbooking.booking.domain.Booking;
import com.hotelbooking.booking.domain.BookingCancellation;
import com.hotelbooking.booking.domain.BookingId;
import com.hotelbooking.cancellation.domain.CancellationContext;
import com.hotelbooking.cancellation.domain.CancellationPolicy;
import com.hotelbooking.cancellation.domain.RefundDecision;
import com.hotelbooking.common.application.StrategyRegistry;
import com.hotelbooking.payment.application.RefundService;
import com.hotelbooking.payment.domain.Payment;
import com.hotelbooking.property.domain.CancellationPolicyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

/**
 * Cancels a booking: in one transaction the booking is cancelled, its rooms are released and a refund is
 * requested per the booking's policy. The refund is then sent to the gateway after the commit.
 */
@Service
public class CancellationService {

    private static final Logger log = LoggerFactory.getLogger(CancellationService.class);

    private final BookingService bookings;
    private final RefundService refunds;
    private final StrategyRegistry<CancellationPolicyType, CancellationPolicy> policies;
    private final TransactionTemplate transactions;

    public CancellationService(BookingService bookings, RefundService refunds, List<CancellationPolicy> policies,
                               PlatformTransactionManager transactionManager) {
        this.bookings = bookings;
        this.refunds = refunds;
        this.policies = new StrategyRegistry<>("cancellation policy", policies, CancellationPolicy::type);
        this.transactions = new TransactionTemplate(transactionManager);
    }

    public CancellationResult cancel(BookingId bookingId) {
        CancellationResult result = transactions.execute(status -> cancelAndRequestRefund(bookingId));
        return result.refundPayment()
                .map(payment -> result.withPayment(refunds.processRefund(payment.id())))
                .orElse(result);
    }

    private CancellationResult cancelAndRequestRefund(BookingId bookingId) {
        BookingCancellation cancellation = bookings.cancel(bookingId);
        Booking booking = cancellation.booking();
        if (!cancellation.wasConfirmed()) {
            return CancellationResult.withoutRefund(booking, RefundDecision.none(booking.totalAmount().currency()));
        }

        RefundDecision decision = policies.get(booking.cancellationPolicy())
                .evaluate(new CancellationContext(booking.totalAmount(), cancellation.noticeBeforeCheckIn()));
        log.info("Booking {} under {} policy: {}% refund ({} of {})", booking.id(), booking.cancellationPolicy(),
                decision.refundPercentage(), decision.refundAmount(), booking.totalAmount());
        if (!decision.isRefundable()) {
            return CancellationResult.withoutRefund(booking, decision);
        }
        Payment payment = refunds.requestRefund(booking.id(), decision.refundAmount());
        return CancellationResult.withRefund(booking, decision, payment);
    }
}
