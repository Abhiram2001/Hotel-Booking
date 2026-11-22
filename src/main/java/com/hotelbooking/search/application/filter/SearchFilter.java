package com.hotelbooking.search.application.filter;

import com.hotelbooking.search.application.RoomOffer;
import com.hotelbooking.search.application.SearchCriteria;

/**
 * One search rule. The search service applies every filter bean that is active for the request, so a new
 * filter is a new class (plus its field in {@link SearchCriteria}); the search itself does not change.
 */
public interface SearchFilter {

    /** Whether the request asks for this filter at all. */
    boolean isActive(SearchCriteria criteria);

    boolean matches(RoomOffer offer, SearchCriteria criteria);
}
