package com.hotelbooking.property.domain;

import com.hotelbooking.common.domain.Guard;

import java.util.UUID;

public record OwnerId(UUID value) {

    public OwnerId {
        Guard.notNull(value, "ownerId");
    }

    public static OwnerId newId() {
        return new OwnerId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
