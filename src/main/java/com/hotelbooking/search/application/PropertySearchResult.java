package com.hotelbooking.search.application;

import com.hotelbooking.common.domain.Money;
import com.hotelbooking.property.domain.Property;

import java.util.List;

/** A property with the room offers that matched every filter. Never empty. */
public record PropertySearchResult(Property property, List<RoomOffer> offers) {

    public PropertySearchResult {
        offers = List.copyOf(offers);
    }

    public Money lowestTotal() {
        return offers.stream().map(offer -> offer.quote().total()).min(Money::compareTo).orElseThrow();
    }
}
