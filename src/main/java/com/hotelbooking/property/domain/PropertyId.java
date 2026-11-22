package com.hotelbooking.property.domain;

import com.hotelbooking.common.domain.Guard;

import java.util.UUID;

public record PropertyId(UUID value) {

    public PropertyId {
        Guard.notNull(value, "propertyId");
    }

    public static PropertyId newId() {
        return new PropertyId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
