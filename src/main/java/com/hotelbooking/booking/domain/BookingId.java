package com.hotelbooking.booking.domain;

import com.hotelbooking.common.domain.Guard;

import java.util.UUID;

public record BookingId(UUID value) {

    public BookingId {
        Guard.notNull(value, "bookingId");
    }

    public static BookingId newId() {
        return new BookingId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
