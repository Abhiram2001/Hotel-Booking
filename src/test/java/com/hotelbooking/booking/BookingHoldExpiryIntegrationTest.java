package com.hotelbooking.booking;

import com.hotelbooking.booking.application.BookingHoldExpiryJob;
import com.hotelbooking.booking.domain.Booking;
import com.hotelbooking.booking.domain.BookingStatus;
import com.hotelbooking.cancellation.application.CancellationService;
import com.hotelbooking.common.domain.error.ConflictException;
import com.hotelbooking.common.domain.error.InvalidStateTransitionException;
import com.hotelbooking.property.domain.CancellationPolicyType;
import com.hotelbooking.property.domain.Property;
import com.hotelbooking.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;

import static com.hotelbooking.support.DomainAssertions.assertRejected;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The scheduled job is disabled in tests; each test runs it explicitly after moving the clock. */
class BookingHoldExpiryIntegrationTest extends IntegrationTestSupport {

    private static final Duration HOLD = Duration.ofMinutes(15);

    @Autowired
    CancellationService cancellations;

    private BookingHoldExpiryJob job;
    private Property property;
    private Booking booking;

    @BeforeEach
    void bookTheOnlyRoom() {
        job = new BookingHoldExpiryJob(bookings);
        property = onboardProperty(CancellationPolicyType.FLEXIBLE, 1);
        booking = book(property, days(3, 5));
    }

    @Test
    void unpaidBookingExpiresAndItsRoomsBecomeBookableAgain() {
        clock.advance(HOLD.plusSeconds(1));

        job.expireStaleHolds();

        assertThat(bookings.get(booking.id()).status()).isEqualTo(BookingStatus.EXPIRED);
        assertThat(availableRooms(property, booking.stay().dates())).isEqualTo(1);
        assertThat(book(property, booking.stay().dates()).status()).isEqualTo(BookingStatus.PENDING_PAYMENT);
    }

    @Test
    void holdIsKeptUntilItsDeadline() {
        clock.advance(HOLD.minusSeconds(1));

        job.expireStaleHolds();

        assertThat(bookings.get(booking.id()).status()).isEqualTo(BookingStatus.PENDING_PAYMENT);
        assertThat(availableRooms(property, booking.stay().dates())).isZero();
    }

    @Test
    void paidBookingIsNeverExpired() {
        payByUpi(booking);
        clock.advance(Duration.ofHours(1));

        job.expireStaleHolds();

        assertThat(bookings.get(booking.id()).status()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(availableRooms(property, booking.stay().dates())).isZero();
    }

    @Test
    void expiredBookingCanNeitherBePaidNorCancelled() {
        clock.advance(HOLD);
        job.expireStaleHolds();

        assertRejected(ConflictException.class, "BOOKING_NOT_PAYABLE", () -> payByUpi(booking));
        assertThatThrownBy(() -> cancellations.cancel(booking.id())).isInstanceOf(InvalidStateTransitionException.class);
        assertThat(gateway.chargeCount()).isZero();
    }
}
