package com.hotelbooking.booking.domain;

import java.time.Duration;

/** Result of cancelling a booking: what state it was in and how much notice the guest gave. */
public record BookingCancellation(Booking booking, BookingStatus previousStatus, Duration noticeBeforeCheckIn) {

    public boolean wasConfirmed() {
        return previousStatus == BookingStatus.CONFIRMED;
    }
}
