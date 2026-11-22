package com.hotelbooking.pricing.domain;

import com.hotelbooking.common.domain.DateRange;
import com.hotelbooking.common.domain.Guard;
import com.hotelbooking.property.domain.Property;
import com.hotelbooking.property.domain.RoomType;

/** The property is included so future strategies can price by location, star rating or owner. */
public record PricingRequest(Property property, RoomType roomType, DateRange stay, int rooms) {

    public PricingRequest {
        Guard.notNull(property, "property");
        Guard.notNull(roomType, "roomType");
        Guard.notNull(stay, "stay");
        Guard.positive(rooms, "rooms");
    }
}
