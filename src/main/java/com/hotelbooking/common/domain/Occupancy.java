package com.hotelbooking.common.domain;

/** How many guests stay and how many rooms they need. Every room must hold at least one guest. */
public record Occupancy(int guests, int rooms) {

    public Occupancy {
        Guard.positive(guests, "guests");
        Guard.positive(rooms, "rooms");
        Guard.isTrue(rooms <= guests, "rooms (%d) cannot exceed guests (%d)".formatted(rooms, guests));
    }

    public static Occupancy of(int guests, int rooms) {
        return new Occupancy(guests, rooms);
    }
}
