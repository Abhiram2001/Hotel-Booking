package com.hotelbooking.search.application.filter;

import com.hotelbooking.search.application.RoomOffer;
import com.hotelbooking.search.application.SearchCriteria;
import org.springframework.stereotype.Component;

/** Always on: the requested rooms of this type must fit all the guests. */
@Component
class OccupancyFilter implements SearchFilter {

    @Override
    public boolean isActive(SearchCriteria criteria) {
        return true;
    }

    @Override
    public boolean matches(RoomOffer offer, SearchCriteria criteria) {
        return offer.roomType().canAccommodate(criteria.occupancy());
    }
}
