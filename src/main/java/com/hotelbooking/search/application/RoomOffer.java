package com.hotelbooking.search.application;

import com.hotelbooking.pricing.domain.PriceQuote;
import com.hotelbooking.property.domain.Property;
import com.hotelbooking.property.domain.RoomType;

/** One room type of one property, priced and checked for availability for the requested stay. */
public record RoomOffer(Property property, RoomType roomType, int availableRooms, PriceQuote quote) {
}
