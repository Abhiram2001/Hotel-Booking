package com.hotelbooking.booking.application;

import com.hotelbooking.booking.domain.BookingId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Releases rooms held by bookings that were never paid. Each booking expires in its own transaction, so one
 * failure does not block the rest of the batch.
 */
@Component
@ConditionalOnProperty(prefix = "hotel.booking.expiry-job", name = "enabled", havingValue = "true", matchIfMissing = true)
public class BookingHoldExpiryJob {

    private static final Logger log = LoggerFactory.getLogger(BookingHoldExpiryJob.class);
    private static final int BATCH_SIZE = 100;

    private final BookingService bookings;

    public BookingHoldExpiryJob(BookingService bookings) {
        this.bookings = bookings;
    }

    @Scheduled(fixedDelayString = "${hotel.booking.expiry-job.interval:PT30S}")
    public void expireStaleHolds() {
        List<BookingId> candidates = bookings.findExpiredHolds(BATCH_SIZE);
        long expired = candidates.stream().filter(this::expireQuietly).count();
        if (expired > 0) {
            log.info("Expired {} unpaid booking hold(s)", expired);
        }
    }

    private boolean expireQuietly(BookingId id) {
        try {
            return bookings.expireHold(id);
        } catch (RuntimeException ex) {
            log.warn("Could not expire booking {}", id, ex);
            return false;
        }
    }
}
