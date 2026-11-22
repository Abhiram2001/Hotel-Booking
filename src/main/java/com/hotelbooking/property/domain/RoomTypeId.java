package com.hotelbooking.property.domain;

import com.hotelbooking.common.domain.Guard;

import java.util.UUID;

public record RoomTypeId(UUID value) {

    public RoomTypeId {
        Guard.notNull(value, "roomTypeId");
    }

    public static RoomTypeId newId() {
        return new RoomTypeId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
