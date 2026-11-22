package com.hotelbooking.cancellation.api;

import com.hotelbooking.booking.domain.Booking;
import com.hotelbooking.booking.domain.BookingStatus;
import com.hotelbooking.cancellation.application.CancellationResult;
import com.hotelbooking.common.api.MoneyDto;
import com.hotelbooking.payment.domain.Payment;
import com.hotelbooking.payment.domain.PaymentStatus;
import com.hotelbooking.property.domain.CancellationPolicyType;

import java.time.Instant;
import java.util.UUID;

/** @param refundStatus status of the refunded payment, or null when there is nothing to refund */
public record CancellationResponse(
        UUID bookingId,
        BookingStatus bookingStatus,
        Instant cancelledAt,
        CancellationPolicyType cancellationPolicy,
        int refundPercentage,
        MoneyDto refundAmount,
        UUID paymentId,
        PaymentStatus refundStatus) {

    static CancellationResponse from(CancellationResult result) {
        Booking booking = result.booking();
        return new CancellationResponse(
                booking.id().value(),
                booking.status(),
                booking.cancelledAt(),
                booking.cancellationPolicy(),
                result.refundDecision().refundPercentage(),
                MoneyDto.from(result.refundDecision().refundAmount()),
                result.refundPayment().map(payment -> payment.id().value()).orElse(null),
                result.refundPayment().map(Payment::status).orElse(null));
    }
}
