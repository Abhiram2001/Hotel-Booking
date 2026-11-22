package com.hotelbooking.search.application.filter;

import com.hotelbooking.search.application.RoomOffer;
import com.hotelbooking.search.application.SearchCriteria;
import org.springframework.stereotype.Component;

/** The property must offer every requested amenity. */
@Component
class AmenitiesFilter implements SearchFilter {

    @Override
    public boolean isActive(SearchCriteria criteria) {
        return !criteria.amenities().isEmpty();
    }

    @Override
    public boolean matches(RoomOffer offer, SearchCriteria criteria) {
        return offer.property().offersAll(criteria.amenities());
    }
}
