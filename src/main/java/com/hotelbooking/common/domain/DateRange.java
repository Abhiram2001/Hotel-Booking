package com.hotelbooking.common.domain;

import com.hotelbooking.common.domain.error.DomainValidationException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * A stay from check-in (inclusive) to check-out (exclusive). The check-out day is not an occupied night,
 * so back-to-back stays on the same room do not overlap.
 */
public record DateRange(LocalDate checkIn, LocalDate checkOut) {

    public DateRange {
        Guard.notNull(checkIn, "checkIn");
        Guard.notNull(checkOut, "checkOut");
        Guard.isTrue(checkOut.isAfter(checkIn), "checkOut must be after checkIn");
    }

    public static DateRange of(LocalDate checkIn, LocalDate checkOut) {
        return new DateRange(checkIn, checkOut);
    }

    public int nights() {
        return (int) ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    /** Every occupied night of the stay, in ascending order. */
    public List<LocalDate> stayDates() {
        return checkIn.datesUntil(checkOut).toList();
    }

    public void ensureNotInPast(LocalDate today) {
        if (checkIn.isBefore(today)) {
            throw new DomainValidationException("CHECK_IN_IN_PAST", "Check-in date %s is in the past".formatted(checkIn));
        }
    }
}
