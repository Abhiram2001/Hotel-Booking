package com.hotelbooking.inventory.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Embeddable
class RoomNightKey implements Serializable {

    @Column(name = "room_type_id", nullable = false)
    private UUID roomTypeId;

    @Column(name = "stay_date", nullable = false)
    private LocalDate stayDate;

    protected RoomNightKey() {
    }

    RoomNightKey(UUID roomTypeId, LocalDate stayDate) {
        this.roomTypeId = roomTypeId;
        this.stayDate = stayDate;
    }

    UUID getRoomTypeId() {
        return roomTypeId;
    }

    LocalDate getStayDate() {
        return stayDate;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof RoomNightKey that
                && Objects.equals(roomTypeId, that.roomTypeId)
                && Objects.equals(stayDate, that.stayDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomTypeId, stayDate);
    }
}
