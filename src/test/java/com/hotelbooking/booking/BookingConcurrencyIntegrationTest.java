package com.hotelbooking.booking;

import com.hotelbooking.booking.domain.Booking;
import com.hotelbooking.common.domain.DateRange;
import com.hotelbooking.common.domain.error.ConflictException;
import com.hotelbooking.property.domain.CancellationPolicyType;
import com.hotelbooking.property.domain.Property;
import com.hotelbooking.support.Concurrently;
import com.hotelbooking.support.Concurrently.Outcome;
import com.hotelbooking.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BookingConcurrencyIntegrationTest extends IntegrationTestSupport {

    private static final int THREADS = 20;

    @Test
    void simultaneousBookingsNeverOversellTheLastRooms() throws Exception {
        Property property = onboardProperty(CancellationPolicyType.FLEXIBLE, 3);
        DateRange stay = days(10, 13);

        List<Outcome<Booking>> outcomes = Concurrently.run(THREADS, index -> book(property, stay));

        assertThat(outcomes).filteredOn(Outcome::succeeded).hasSize(3);
        assertThat(outcomes).filteredOn(outcome -> !outcome.succeeded())
                .hasSize(THREADS - 3)
                .allSatisfy(outcome -> assertThat(outcome.error()).isInstanceOf(ConflictException.class));
        assertThat(availableRooms(property, stay)).isZero();
    }

    @Test
    void overlappingStaysCompeteOnlyForTheSharedNights() throws Exception {
        Property property = onboardProperty(CancellationPolicyType.FLEXIBLE, 1);
        // Even threads want nights 10-11, odd threads want nights 12-13: the two groups share no night.
        DateRange early = days(10, 12);
        DateRange late = days(12, 14);

        List<Outcome<Booking>> outcomes = Concurrently.run(THREADS,
                index -> book(property, index % 2 == 0 ? early : late));

        assertThat(outcomes).filteredOn(Outcome::succeeded).hasSize(2);
        assertThat(availableRooms(property, early)).isZero();
        assertThat(availableRooms(property, late)).isZero();
    }
}
