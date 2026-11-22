package com.hotelbooking.booking.domain;

import com.hotelbooking.common.domain.DateRange;
import com.hotelbooking.common.domain.Guard;
import com.hotelbooking.common.domain.Occupancy;
import com.hotelbooking.property.domain.PropertyId;
import com.hotelbooking.property.domain.RoomTypeId;

/** What was booked: which rooms, for which nights, for how many guests. */
public record Stay(PropertyId propertyId, RoomTypeId roomTypeId, DateRange dates, Occupancy occupancy) {

    public Stay {
        Guard.notNull(propertyId, "propertyId");
        Guard.notNull(roomTypeId, "roomTypeId");
        Guard.notNull(dates, "dates");
        Guard.notNull(occupancy, "occupancy");
    }
}
