package com.hotelbooking.booking.domain;

import com.hotelbooking.common.domain.DateRange;
import com.hotelbooking.common.domain.Money;
import com.hotelbooking.common.domain.Occupancy;
import com.hotelbooking.common.domain.error.BusinessRuleViolationException;
import com.hotelbooking.common.domain.error.ConflictException;
import com.hotelbooking.common.domain.error.InvalidStateTransitionException;
import com.hotelbooking.property.domain.CancellationPolicyType;
import com.hotelbooking.property.domain.PropertyId;
import com.hotelbooking.property.domain.RoomTypeId;
import com.hotelbooking.support.TestData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;

import static com.hotelbooking.support.DomainAssertions.assertRejected;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingTest {

    private static final Instant NOW = Instant.parse("2030-06-01T04:30:00Z");
    private static final Duration HOLD = Duration.ofMinutes(15);
    private static final Instant CHECK_IN_AT = Instant.parse("2030-06-10T08:30:00Z");

    private final Booking booking = Booking.place(stay(), TestData.guest(), Money.inr(8000),
            CancellationPolicyType.MODERATE, NOW, HOLD);

    @Test
    void newBookingAwaitsPaymentUntilTheHoldExpires() {
        assertThat(booking.status()).isEqualTo(BookingStatus.PENDING_PAYMENT);
        assertThat(booking.holdExpiresAt()).isEqualTo(NOW.plus(HOLD));
        assertThat(booking.isHoldExpired(NOW.plus(HOLD).minusSeconds(1))).isFalse();
        assertThat(booking.isHoldExpired(NOW.plus(HOLD))).isTrue();
    }

    @Test
    void successfulPaymentConfirmsTheBooking() {
        booking.confirm(NOW.plusSeconds(60));

        assertThat(booking.status()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(booking.confirmedAt()).isEqualTo(NOW.plusSeconds(60));
    }

    @Test
    void onlyAnUnpaidBookingWithAValidHoldCanBePaid() {
        assertRejected(ConflictException.class, "BOOKING_HOLD_EXPIRED", () -> booking.ensureAwaitingPayment(NOW.plus(HOLD)));

        booking.confirm(NOW);
        assertRejected(ConflictException.class, "BOOKING_NOT_PAYABLE", () -> booking.ensureAwaitingPayment(NOW));
    }

    @Test
    void confirmedBookingCannotBeConfirmedAgainOrExpire() {
        booking.confirm(NOW);

        assertThatThrownBy(() -> booking.confirm(NOW)).isInstanceOf(InvalidStateTransitionException.class);
        assertThatThrownBy(() -> booking.expireHold(NOW.plus(HOLD))).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void cancellationRecordsPreviousStatusAndNoticeGiven() {
        booking.confirm(NOW);
        Instant cancelAt = CHECK_IN_AT.minus(Duration.ofHours(30));

        BookingCancellation cancellation = booking.cancel(cancelAt, CHECK_IN_AT);

        assertThat(booking.status()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(booking.cancelledAt()).isEqualTo(cancelAt);
        assertThat(cancellation.wasConfirmed()).isTrue();
        assertThat(cancellation.noticeBeforeCheckIn()).isEqualTo(Duration.ofHours(30));
    }

    @Test
    void cannotCancelOnceCheckInTimeHasPassed() {
        booking.confirm(NOW);

        assertRejected(BusinessRuleViolationException.class, "CANCELLATION_WINDOW_CLOSED",
                () -> booking.cancel(CHECK_IN_AT, CHECK_IN_AT));
        assertThat(booking.status()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void holdExpiresOnlyAfterItsDeadline() {
        assertRejected(BusinessRuleViolationException.class, "HOLD_STILL_ACTIVE", () -> booking.expireHold(NOW));

        booking.expireHold(NOW.plus(HOLD));

        assertThat(booking.status()).isEqualTo(BookingStatus.EXPIRED);
    }

    @ParameterizedTest
    @EnumSource(value = BookingStatus.class, names = {"CANCELLED", "EXPIRED"})
    void cancelledAndExpiredAreFinal(BookingStatus terminal) {
        assertThat(Arrays.stream(BookingStatus.values())).noneMatch(terminal::canTransitionTo);
    }

    private static Stay stay() {
        return new Stay(PropertyId.newId(), RoomTypeId.newId(),
                DateRange.of(LocalDate.of(2030, 6, 10), LocalDate.of(2030, 6, 12)), Occupancy.of(2, 1));
    }
}
