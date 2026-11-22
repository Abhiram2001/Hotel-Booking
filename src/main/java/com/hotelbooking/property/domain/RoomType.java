package com.hotelbooking.property.domain;

import com.hotelbooking.common.domain.Guard;
import com.hotelbooking.common.domain.Money;
import com.hotelbooking.common.domain.Occupancy;
import com.hotelbooking.common.domain.error.DomainValidationException;

import java.util.Objects;

/** A category of identical rooms in a property, e.g. "Deluxe King": capacity, room count and base rate. */
public final class RoomType {

    private final RoomTypeId id;
    private final String name;
    private final int maxOccupancy;
    private final int totalRooms;
    private final Money basePricePerNight;

    private RoomType(RoomTypeId id, String name, int maxOccupancy, int totalRooms, Money basePricePerNight) {
        this.id = Guard.notNull(id, "roomTypeId");
        this.name = Guard.notBlank(name, "room type name");
        this.maxOccupancy = Guard.positive(maxOccupancy, "maxOccupancy");
        this.totalRooms = Guard.positive(totalRooms, "totalRooms");
        this.basePricePerNight = Guard.notNull(basePricePerNight, "basePricePerNight");
        Guard.isTrue(!basePricePerNight.isZero(), "basePricePerNight must be greater than zero");
    }

    public static RoomType create(String name, int maxOccupancy, int totalRooms, Money basePricePerNight) {
        return new RoomType(RoomTypeId.newId(), name, maxOccupancy, totalRooms, basePricePerNight);
    }

    public static RoomType restore(RoomTypeId id, String name, int maxOccupancy, int totalRooms, Money basePricePerNight) {
        return new RoomType(id, name, maxOccupancy, totalRooms, basePricePerNight);
    }

    public boolean canAccommodate(Occupancy occupancy) {
        return occupancy.guests() <= (long) maxOccupancy * occupancy.rooms();
    }

    public void ensureCanAccommodate(Occupancy occupancy) {
        if (!canAccommodate(occupancy)) {
            throw new DomainValidationException("OCCUPANCY_EXCEEDED",
                    "%d room(s) of type '%s' fit at most %d guests, requested %d".formatted(
                            occupancy.rooms(), name, maxOccupancy * occupancy.rooms(), occupancy.guests()));
        }
    }

    public RoomTypeId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public int maxOccupancy() {
        return maxOccupancy;
    }

    public int totalRooms() {
        return totalRooms;
    }

    public Money basePricePerNight() {
        return basePricePerNight;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof RoomType that && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
