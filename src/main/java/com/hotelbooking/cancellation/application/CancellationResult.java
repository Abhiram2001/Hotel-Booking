package com.hotelbooking.cancellation.application;

import com.hotelbooking.booking.domain.Booking;
import com.hotelbooking.cancellation.domain.RefundDecision;
import com.hotelbooking.payment.domain.Payment;

import java.util.Optional;

/**
 * @param refundPayment the payment being refunded; empty when nothing was paid or nothing is refundable
 */
public record CancellationResult(Booking booking, RefundDecision refundDecision, Optional<Payment> refundPayment) {

    static CancellationResult withoutRefund(Booking booking, RefundDecision decision) {
        return new CancellationResult(booking, decision, Optional.empty());
    }

    static CancellationResult withRefund(Booking booking, RefundDecision decision, Payment payment) {
        return new CancellationResult(booking, decision, Optional.of(payment));
    }

    CancellationResult withPayment(Payment payment) {
        return new CancellationResult(booking, refundDecision, Optional.of(payment));
    }
}
