package com.hotelbooking.booking.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BookingRepository {

    Booking save(Booking booking);

    Optional<Booking> findById(BookingId id);

    /**
     * Loads the booking and locks its row for the current transaction. Payment, cancellation and expiry all
     * take this lock first, so only one of them can change a given booking at a time.
     */
    Optional<Booking> findByIdForUpdate(BookingId id);

    List<BookingId> findExpiredHolds(Instant now, int limit);
}
