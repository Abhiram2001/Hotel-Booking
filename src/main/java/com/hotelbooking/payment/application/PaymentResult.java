package com.hotelbooking.payment.application;

import com.hotelbooking.payment.domain.Payment;

/** @param replayed true when an earlier result was returned for a repeated Idempotency-Key */
public record PaymentResult(Payment payment, boolean replayed) {

    static PaymentResult created(Payment payment) {
        return new PaymentResult(payment, false);
    }

    static PaymentResult replayed(Payment payment) {
        return new PaymentResult(payment, true);
    }
}
