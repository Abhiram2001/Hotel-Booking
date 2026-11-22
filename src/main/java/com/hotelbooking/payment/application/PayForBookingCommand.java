package com.hotelbooking.payment.application;

import com.hotelbooking.booking.domain.BookingId;
import com.hotelbooking.common.domain.Guard;
import com.hotelbooking.payment.domain.IdempotencyKey;
import com.hotelbooking.payment.domain.PaymentInstrument;

public record PayForBookingCommand(BookingId bookingId, PaymentInstrument instrument, IdempotencyKey idempotencyKey) {

    public PayForBookingCommand {
        Guard.notNull(bookingId, "bookingId");
        Guard.notNull(instrument, "payment instrument");
        Guard.notNull(idempotencyKey, "Idempotency-Key");
    }
}
