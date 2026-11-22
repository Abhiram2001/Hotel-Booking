package com.hotelbooking.search.application;

import com.hotelbooking.common.domain.DateRange;
import com.hotelbooking.common.domain.Guard;
import com.hotelbooking.common.domain.Money;
import com.hotelbooking.common.domain.Occupancy;
import com.hotelbooking.property.domain.Amenity;

import java.util.Set;

/**
 * What the guest is looking for. City, dates and occupancy are required; the rest are optional filters
 * and are null (or empty) when not requested. Price bounds apply to the average nightly rate per room.
 */
public record SearchCriteria(String city, DateRange stay, Occupancy occupancy, String locality,
                             Money minNightlyRate, Money maxNightlyRate, Set<Amenity> amenities,
                             Integer minStarRating) {

    public SearchCriteria {
        city = Guard.notBlank(city, "city");
        Guard.notNull(stay, "stay");
        Guard.notNull(occupancy, "occupancy");
        amenities = amenities == null ? Set.of() : Set.copyOf(amenities);
        if (minNightlyRate != null && maxNightlyRate != null) {
            Guard.isTrue(!minNightlyRate.isGreaterThan(maxNightlyRate), "minPrice cannot be greater than maxPrice");
        }
    }

    public static SearchCriteria of(String city, DateRange stay, Occupancy occupancy) {
        return new SearchCriteria(city, stay, occupancy, null, null, null, Set.of(), null);
    }
}
