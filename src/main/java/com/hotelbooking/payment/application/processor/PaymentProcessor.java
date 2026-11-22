package com.hotelbooking.payment.application.processor;

import com.hotelbooking.payment.domain.Payment;
import com.hotelbooking.payment.domain.PaymentInstrument;
import com.hotelbooking.payment.domain.PaymentMethod;
import com.hotelbooking.payment.domain.gateway.GatewayResult;

/**
 * Strategy for one payment method. To add a method (net banking, BNPL, ...), add an instrument type and a
 * processor bean; the payment flow picks it up through {@link PaymentProcessors} without changes.
 */
public interface PaymentProcessor {

    PaymentMethod method();

    /** Method-specific checks that must pass before a payment attempt is recorded. */
    void validate(PaymentInstrument instrument);

    GatewayResult charge(Payment payment, PaymentInstrument instrument);

    GatewayResult refund(Payment payment);
}
