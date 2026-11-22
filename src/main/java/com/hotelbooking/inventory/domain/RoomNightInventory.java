package com.hotelbooking.inventory.domain;

import com.hotelbooking.common.domain.Guard;
import com.hotelbooking.common.domain.error.ConflictException;
import com.hotelbooking.property.domain.RoomTypeId;

import java.time.LocalDate;

/**
 * How many rooms of one room type are sold for one night. Availability is tracked per night, so a stay is
 * bookable only when every night in it has enough free rooms.
 */
public final class RoomNightInventory {

    private final RoomTypeId roomTypeId;
    private final LocalDate date;
    private final int totalRooms;
    private int bookedRooms;

    public RoomNightInventory(RoomTypeId roomTypeId, LocalDate date, int totalRooms, int bookedRooms) {
        this.roomTypeId = Guard.notNull(roomTypeId, "roomTypeId");
        this.date = Guard.notNull(date, "date");
        this.totalRooms = Guard.positive(totalRooms, "totalRooms");
        Guard.isTrue(bookedRooms >= 0 && bookedRooms <= totalRooms, "bookedRooms must be between 0 and totalRooms");
        this.bookedRooms = bookedRooms;
    }

    public static RoomNightInventory open(RoomTypeId roomTypeId, LocalDate date, int totalRooms) {
        return new RoomNightInventory(roomTypeId, date, totalRooms, 0);
    }

    public int availableRooms() {
        return totalRooms - bookedRooms;
    }

    public void reserve(int rooms) {
        Guard.positive(rooms, "rooms");
        if (rooms > availableRooms()) {
            throw new ConflictException("ROOMS_UNAVAILABLE",
                    "Only %d room(s) left on %s, requested %d".formatted(availableRooms(), date, rooms));
        }
        bookedRooms += rooms;
    }

    public void release(int rooms) {
        Guard.positive(rooms, "rooms");
        if (rooms > bookedRooms) {
            throw new IllegalStateException("Cannot release %d room(s) on %s for room type %s: only %d booked"
                    .formatted(rooms, date, roomTypeId, bookedRooms));
        }
        bookedRooms -= rooms;
    }

    public RoomTypeId roomTypeId() {
        return roomTypeId;
    }

    public LocalDate date() {
        return date;
    }

    public int totalRooms() {
        return totalRooms;
    }

    public int bookedRooms() {
        return bookedRooms;
    }
}
