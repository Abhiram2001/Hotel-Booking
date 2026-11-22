package com.hotelbooking.inventory.domain;

import com.hotelbooking.common.domain.error.ConflictException;
import com.hotelbooking.property.domain.RoomTypeId;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.hotelbooking.support.DomainAssertions.assertRejected;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoomNightInventoryTest {

    private final RoomNightInventory night = RoomNightInventory.open(RoomTypeId.newId(), LocalDate.of(2030, 6, 10), 3);

    @Test
    void reservingAndReleasingMovesAvailability() {
        night.reserve(2);
        assertThat(night.availableRooms()).isEqualTo(1);

        night.release(2);
        assertThat(night.availableRooms()).isEqualTo(3);
    }

    @Test
    void cannotReserveMoreThanIsLeft() {
        night.reserve(2);

        assertRejected(ConflictException.class, "ROOMS_UNAVAILABLE", () -> night.reserve(2));
        assertThat(night.bookedRooms()).isEqualTo(2);
    }

    @Test
    void releasingMoreThanWasBookedIsABug() {
        night.reserve(1);

        assertThatThrownBy(() -> night.release(2)).isInstanceOf(IllegalStateException.class);
        assertThat(night.bookedRooms()).isEqualTo(1);
    }
}
