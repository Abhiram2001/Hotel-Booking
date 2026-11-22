package com.hotelbooking.common.domain;

import com.hotelbooking.common.domain.error.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.hotelbooking.support.DomainAssertions.assertRejected;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DateRangeTest {

    private static final LocalDate JUNE_10 = LocalDate.of(2030, 6, 10);

    @Test
    void checkOutDayIsNotAnOccupiedNight() {
        DateRange stay = DateRange.of(JUNE_10, JUNE_10.plusDays(3));

        assertThat(stay.nights()).isEqualTo(3);
        assertThat(stay.stayDates()).containsExactly(JUNE_10, JUNE_10.plusDays(1), JUNE_10.plusDays(2));
    }

    @Test
    void checkOutMustBeAfterCheckIn() {
        assertThatThrownBy(() -> DateRange.of(JUNE_10, JUNE_10)).isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> DateRange.of(JUNE_10, JUNE_10.minusDays(1))).isInstanceOf(DomainValidationException.class);
    }

    @Test
    void checkInTodayIsAllowedButNotInThePast() {
        DateRange stay = DateRange.of(JUNE_10, JUNE_10.plusDays(1));

        assertThatCode(() -> stay.ensureNotInPast(JUNE_10)).doesNotThrowAnyException();
        assertRejected(DomainValidationException.class, "CHECK_IN_IN_PAST", () -> stay.ensureNotInPast(JUNE_10.plusDays(1)));
    }

    @Test
    void occupancyNeedsAtLeastOneGuestPerRoom() {
        assertThatCode(() -> Occupancy.of(3, 3)).doesNotThrowAnyException();
        assertThatThrownBy(() -> Occupancy.of(2, 3)).isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> Occupancy.of(0, 1)).isInstanceOf(DomainValidationException.class);
    }
}
