package com.hotelbooking.payment.api.dto;

import com.hotelbooking.common.api.MoneyDto;
import com.hotelbooking.payment.domain.Payment;
import com.hotelbooking.payment.domain.PaymentMethod;
import com.hotelbooking.payment.domain.PaymentStatus;

import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID paymentId,
        UUID bookingId,
        PaymentStatus status,
        MoneyDto amount,
        PaymentMethod method,
        String instrument,
        String gatewayReference,
        String failureReason,
        MoneyDto refundAmount,
        String refundReference,
        Instant createdAt) {

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.id().value(),
                payment.bookingId().value(),
                payment.status(),
                MoneyDto.from(payment.amount()),
                payment.method(),
                payment.maskedInstrument(),
                payment.gatewayReference(),
                payment.failureReason(),
                MoneyDto.from(payment.refundAmount()),
                payment.refundReference(),
                payment.createdAt());
    }
}
