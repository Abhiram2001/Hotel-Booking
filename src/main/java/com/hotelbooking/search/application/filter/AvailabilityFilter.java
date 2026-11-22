package com.hotelbooking.search.application.filter;

import com.hotelbooking.search.application.RoomOffer;
import com.hotelbooking.search.application.SearchCriteria;
import org.springframework.stereotype.Component;

/** Always on: only offers with enough free rooms on every night of the stay are shown. */
@Component
class AvailabilityFilter implements SearchFilter {

    @Override
    public boolean isActive(SearchCriteria criteria) {
        return true;
    }

    @Override
    public boolean matches(RoomOffer offer, SearchCriteria criteria) {
        return offer.availableRooms() >= criteria.occupancy().rooms();
    }
}
