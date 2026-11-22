package com.hotelbooking.search.application.filter;

import com.hotelbooking.search.application.RoomOffer;
import com.hotelbooking.search.application.SearchCriteria;
import org.springframework.stereotype.Component;

@Component
class StarRatingFilter implements SearchFilter {

    @Override
    public boolean isActive(SearchCriteria criteria) {
        return criteria.minStarRating() != null;
    }

    @Override
    public boolean matches(RoomOffer offer, SearchCriteria criteria) {
        return offer.property().starRating().isAtLeast(criteria.minStarRating());
    }
}
