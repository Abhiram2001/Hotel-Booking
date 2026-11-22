package com.hotelbooking.search.application.filter;

import com.hotelbooking.common.domain.DateRange;
import com.hotelbooking.common.domain.Money;
import com.hotelbooking.common.domain.Occupancy;
import com.hotelbooking.pricing.domain.BasePricingStrategy;
import com.hotelbooking.pricing.domain.PricingRequest;
import com.hotelbooking.property.domain.Amenity;
import com.hotelbooking.property.domain.CancellationPolicyType;
import com.hotelbooking.property.domain.Location;
import com.hotelbooking.property.domain.OwnerId;
import com.hotelbooking.property.domain.Property;
import com.hotelbooking.property.domain.RoomType;
import com.hotelbooking.property.domain.StarRating;
import com.hotelbooking.search.application.RoomOffer;
import com.hotelbooking.search.application.SearchCriteria;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/** A 4-star property in Koramangala with WIFI and POOL, one room type: 2 guests per room, 3000 per night. */
class SearchFiltersTest {

    private static final DateRange STAY = DateRange.of(LocalDate.of(2030, 6, 10), LocalDate.of(2030, 6, 12));
    private static final Occupancy ONE_ROOM = Occupancy.of(2, 1);

    private final RoomOffer offer = offer(2);

    @Test
    void optionalFiltersAreInactiveUntilRequested() {
        SearchCriteria plain = SearchCriteria.of("Bengaluru", STAY, ONE_ROOM);

        assertThat(List.of(new LocalityFilter(), new PriceRangeFilter(), new AmenitiesFilter(), new StarRatingFilter()))
                .noneMatch(filter -> filter.isActive(plain));
        assertThat(List.of(new AvailabilityFilter(), new OccupancyFilter())).allMatch(filter -> filter.isActive(plain));
    }

    @Test
    void availabilityNeedsEnoughFreeRoomsForTheWholeStay() {
        AvailabilityFilter filter = new AvailabilityFilter();

        assertThat(filter.matches(offer, criteria(Occupancy.of(2, 2), null, null, null, Set.of(), null))).isTrue();
        assertThat(filter.matches(offer, criteria(Occupancy.of(3, 3), null, null, null, Set.of(), null))).isFalse();
    }

    @Test
    void occupancyNeedsTheRequestedRoomsToFitEveryGuest() {
        OccupancyFilter filter = new OccupancyFilter();

        assertThat(filter.matches(offer, criteria(Occupancy.of(4, 2), null, null, null, Set.of(), null))).isTrue();
        assertThat(filter.matches(offer, criteria(Occupancy.of(3, 1), null, null, null, Set.of(), null))).isFalse();
    }

    @Test
    void localityMatchesIgnoringCase() {
        LocalityFilter filter = new LocalityFilter();

        assertThat(filter.matches(offer, criteria(ONE_ROOM, " koramangala ", null, null, Set.of(), null))).isTrue();
        assertThat(filter.matches(offer, criteria(ONE_ROOM, "Indiranagar", null, null, Set.of(), null))).isFalse();
    }

    @Test
    void priceRangeUsesTheAverageNightlyRateWithInclusiveBounds() {
        PriceRangeFilter filter = new PriceRangeFilter();

        assertThat(filter.matches(offer, criteria(ONE_ROOM, null, Money.inr(3000), Money.inr(3000), Set.of(), null))).isTrue();
        assertThat(filter.matches(offer, criteria(ONE_ROOM, null, null, Money.inr(2999), Set.of(), null))).isFalse();
        assertThat(filter.matches(offer, criteria(ONE_ROOM, null, Money.inr(3001), null, Set.of(), null))).isFalse();
    }

    @Test
    void amenitiesMustAllBeOffered() {
        AmenitiesFilter filter = new AmenitiesFilter();

        assertThat(filter.matches(offer, criteria(ONE_ROOM, null, null, null, Set.of(Amenity.WIFI, Amenity.SWIMMING_POOL), null))).isTrue();
        assertThat(filter.matches(offer, criteria(ONE_ROOM, null, null, null, Set.of(Amenity.WIFI, Amenity.GYM), null))).isFalse();
    }

    @Test
    void starRatingIsAMinimum() {
        StarRatingFilter filter = new StarRatingFilter();

        assertThat(filter.matches(offer, criteria(ONE_ROOM, null, null, null, Set.of(), 4))).isTrue();
        assertThat(filter.matches(offer, criteria(ONE_ROOM, null, null, null, Set.of(), 5))).isFalse();
    }

    private static SearchCriteria criteria(Occupancy occupancy, String locality, Money min, Money max,
                                           Set<Amenity> amenities, Integer minStars) {
        return new SearchCriteria("Bengaluru", STAY, occupancy, locality, min, max, amenities, minStars);
    }

    private static RoomOffer offer(int availableRooms) {
        RoomType roomType = RoomType.create("Deluxe", 2, 5, Money.inr(3000));
        Property property = Property.onboard(OwnerId.newId(), "Lemon Leaf", new Location("80 Feet Road", "Koramangala", "Bengaluru"),
                StarRating.of(4), Set.of(Amenity.WIFI, Amenity.SWIMMING_POOL), List.of(roomType), CancellationPolicyType.FLEXIBLE);
        return new RoomOffer(property, roomType, availableRooms,
                new BasePricingStrategy().quote(new PricingRequest(property, roomType, STAY, 1)));
    }
}
