package com.hotelbooking.search.application.filter;

import com.hotelbooking.search.application.RoomOffer;
import com.hotelbooking.search.application.SearchCriteria;
import org.springframework.stereotype.Component;

@Component
class LocalityFilter implements SearchFilter {

    @Override
    public boolean isActive(SearchCriteria criteria) {
        return criteria.locality() != null && !criteria.locality().isBlank();
    }

    @Override
    public boolean matches(RoomOffer offer, SearchCriteria criteria) {
        return offer.property().location().isInLocality(criteria.locality());
    }
}
