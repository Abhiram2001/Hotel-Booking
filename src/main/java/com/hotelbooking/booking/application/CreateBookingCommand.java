package com.hotelbooking.booking.application;

import com.hotelbooking.booking.domain.GuestDetails;
import com.hotelbooking.common.domain.DateRange;
import com.hotelbooking.common.domain.Occupancy;
import com.hotelbooking.property.domain.PropertyId;
import com.hotelbooking.property.domain.RoomTypeId;

public record CreateBookingCommand(PropertyId propertyId, RoomTypeId roomTypeId, DateRange dates, Occupancy occupancy,
                                   GuestDetails guest) {
}
