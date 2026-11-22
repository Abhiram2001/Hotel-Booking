package com.hotelbooking.inventory.infrastructure.persistence;

import com.hotelbooking.common.infrastructure.persistence.AssignedIdEntity;
import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * One row per room type per night. The CHECK constraint is a last line of defence: even if application
 * locking were bypassed, the database refuses to oversell a night.
 */
@Entity
@Table(name = "room_night_inventory", check = @CheckConstraint(
        name = "ck_room_night_capacity",
        constraint = "booked_rooms >= 0 AND booked_rooms <= total_rooms"))
class RoomNightEntity extends AssignedIdEntity<RoomNightKey> {

    @EmbeddedId
    private RoomNightKey id;

    @Column(name = "total_rooms", nullable = false)
    private int totalRooms;

    @Column(name = "booked_rooms", nullable = false)
    private int bookedRooms;

    protected RoomNightEntity() {
    }

    RoomNightEntity(RoomNightKey id, int totalRooms, int bookedRooms) {
        this.id = id;
        this.totalRooms = totalRooms;
        this.bookedRooms = bookedRooms;
    }

    @Override
    public RoomNightKey getId() {
        return id;
    }

    int getTotalRooms() {
        return totalRooms;
    }

    int getBookedRooms() {
        return bookedRooms;
    }

    void setBookedRooms(int bookedRooms) {
        this.bookedRooms = bookedRooms;
    }
}
