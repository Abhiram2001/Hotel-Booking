package com.hotelbooking.support;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicReference;

/** A clock tests can move forward, so hold expiry and cancellation windows can be exercised. */
public final class MutableClock extends Clock {

    public static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");
    /** 10:00 IST on 1 June 2030. */
    public static final Instant START = Instant.parse("2030-06-01T04:30:00Z");

    private final AtomicReference<Instant> now = new AtomicReference<>(START);

    @Override
    public ZoneId getZone() {
        return ZONE;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        throw new UnsupportedOperationException("Tests use a single zone");
    }

    @Override
    public Instant instant() {
        return now.get();
    }

    public LocalDate today() {
        return LocalDate.ofInstant(instant(), ZONE);
    }

    public void advance(Duration duration) {
        now.updateAndGet(current -> current.plus(duration));
    }

    public void reset() {
        now.set(START);
    }
}
