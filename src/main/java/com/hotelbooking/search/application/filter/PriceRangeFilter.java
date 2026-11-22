package com.hotelbooking.search.application.filter;

import com.hotelbooking.common.domain.Money;
import com.hotelbooking.search.application.RoomOffer;
import com.hotelbooking.search.application.SearchCriteria;
import org.springframework.stereotype.Component;

/** Compares the average nightly rate per room, so stays of different lengths are filtered the same way. */
@Component
class PriceRangeFilter implements SearchFilter {

    @Override
    public boolean isActive(SearchCriteria criteria) {
        return criteria.minNightlyRate() != null || criteria.maxNightlyRate() != null;
    }

    @Override
    public boolean matches(RoomOffer offer, SearchCriteria criteria) {
        Money rate = offer.quote().averageNightlyRate();
        boolean aboveMin = criteria.minNightlyRate() == null || !rate.isLessThan(criteria.minNightlyRate());
        boolean belowMax = criteria.maxNightlyRate() == null || !rate.isGreaterThan(criteria.maxNightlyRate());
        return aboveMin && belowMax;
    }
}
