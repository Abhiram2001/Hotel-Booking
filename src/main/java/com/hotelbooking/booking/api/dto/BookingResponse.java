package com.hotelbooking.booking.api.dto;

import com.hotelbooking.booking.domain.Booking;
import com.hotelbooking.booking.domain.BookingStatus;
import com.hotelbooking.booking.domain.Stay;
import com.hotelbooking.common.api.MoneyDto;
import com.hotelbooking.property.domain.CancellationPolicyType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BookingResponse(
        UUID bookingId,
        BookingStatus status,
        UUID propertyId,
        UUID roomTypeId,
        LocalDate checkIn,
        LocalDate checkOut,
        int nights,
        int guests,
        int rooms,
        String guestName,
        MoneyDto totalAmount,
        CancellationPolicyType cancellationPolicy,
        Instant createdAt,
        Instant holdExpiresAt,
        Instant confirmedAt,
        Instant cancelledAt) {

    public static BookingResponse from(Booking booking) {
        Stay stay = booking.stay();
        return new BookingResponse(
                booking.id().value(),
                booking.status(),
                stay.propertyId().value(),
                stay.roomTypeId().value(),
                stay.dates().checkIn(),
                stay.dates().checkOut(),
                stay.dates().nights(),
                stay.occupancy().guests(),
                stay.occupancy().rooms(),
                booking.guest().fullName(),
                MoneyDto.from(booking.totalAmount()),
                booking.cancellationPolicy(),
                booking.createdAt(),
                booking.holdExpiresAt(),
                booking.confirmedAt(),
                booking.cancelledAt());
    }
}
