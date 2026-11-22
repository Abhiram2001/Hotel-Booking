package com.hotelbooking.payment.domain;

import com.hotelbooking.common.domain.Guard;

import java.util.UUID;

public record PaymentId(UUID value) {

    public PaymentId {
        Guard.notNull(value, "paymentId");
    }

    public static PaymentId newId() {
        return new PaymentId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
