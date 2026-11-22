package com.hotelbooking.search;

import com.hotelbooking.common.domain.DateRange;
import com.hotelbooking.common.domain.Money;
import com.hotelbooking.common.domain.Occupancy;
import com.hotelbooking.common.domain.error.DomainValidationException;
import com.hotelbooking.property.application.PropertyDraft;
import com.hotelbooking.property.application.RoomTypeDraft;
import com.hotelbooking.property.domain.Amenity;
import com.hotelbooking.property.domain.CancellationPolicyType;
import com.hotelbooking.property.domain.Location;
import com.hotelbooking.property.domain.Property;
import com.hotelbooking.property.domain.StarRating;
import com.hotelbooking.search.application.PropertySearchResult;
import com.hotelbooking.search.application.PropertySearchService;
import com.hotelbooking.search.application.SearchCriteria;
import com.hotelbooking.support.IntegrationTestSupport;
import com.hotelbooking.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

import static com.hotelbooking.support.DomainAssertions.assertRejected;
import static org.assertj.core.api.Assertions.assertThat;

class PropertySearchIntegrationTest extends IntegrationTestSupport {

    private static final Occupancy TWO_GUESTS = Occupancy.of(2, 1);

    @Autowired
    PropertySearchService search;

    private String city;

    @BeforeEach
    void pickACity() {
        city = TestData.uniqueCity();
    }

    @Test
    void showsOnlyRoomTypesFreeOnEveryNightOfTheStay() {
        Property hotel = onboard("Hotel", "Central", 4, Set.of(Amenity.WIFI),
                room("Deluxe", 1, 3000), room("Suite", 1, 6000));
        bookings.create(TestData.booking(hotel, days(10, 12), Occupancy.of(1, 1)));

        assertThat(roomTypesOffered(search(days(11, 13)))).containsExactly("Suite");
        assertThat(roomTypesOffered(search(days(12, 14)))).containsExactlyInAnyOrder("Deluxe", "Suite");
    }

    @Test
    void propertyWithNothingAvailableIsNotReturned() {
        Property hotel = onboard("Hotel", "Central", 4, Set.of(Amenity.WIFI), room("Deluxe", 1, 3000));
        book(hotel, days(10, 12));

        assertThat(search(days(10, 12))).isEmpty();
        assertThat(search(days(9, 10))).hasSize(1);
    }

    @Test
    void resultsAreSortedByLowestPriceAndNarrowedByEachFilter() {
        onboard("Budget Inn", "Central", 3, Set.of(Amenity.WIFI), room("Standard", 5, 2000));
        onboard("Beach Resort", "Beach", 5, Set.of(Amenity.WIFI, Amenity.SWIMMING_POOL), room("Villa", 5, 8000));
        onboard("City Hotel", "Central", 4, Set.of(Amenity.WIFI, Amenity.GYM), room("Deluxe", 5, 5000));
        DateRange stay = days(10, 12);

        assertThat(names(search(stay))).containsExactly("Budget Inn", "City Hotel", "Beach Resort");
        assertThat(names(search(criteria(stay, null, null, null, Set.of(), 4)))).containsExactly("City Hotel", "Beach Resort");
        assertThat(names(search(criteria(stay, null, null, null, Set.of(Amenity.SWIMMING_POOL), null)))).containsExactly("Beach Resort");
        assertThat(names(search(criteria(stay, null, Money.inr(3000), Money.inr(6000), Set.of(), null)))).containsExactly("City Hotel");
        assertThat(names(search(criteria(stay, "beach", null, null, Set.of(), null)))).containsExactly("Beach Resort");
    }

    @Test
    void multiRoomSearchNeedsRoomsThatFitEveryoneAndAreAllFree() {
        onboard("Hotel", "Central", 4, Set.of(Amenity.WIFI), room("Deluxe", 3, 3000));
        DateRange stay = days(10, 12);

        assertThat(search(new SearchCriteria(city, stay, Occupancy.of(5, 2), null, null, null, Set.of(), null))).isEmpty();
        assertThat(search(new SearchCriteria(city, stay, Occupancy.of(5, 3), null, null, null, Set.of(), null))).hasSize(1);
        assertThat(search(new SearchCriteria(city, stay, Occupancy.of(4, 4), null, null, null, Set.of(), null))).isEmpty();
    }

    @Test
    void cityMatchIgnoresCase() {
        onboard("Hotel", "Central", 4, Set.of(Amenity.WIFI), room("Deluxe", 1, 3000));

        assertThat(search.search(SearchCriteria.of(city.toUpperCase(), days(1, 2), TWO_GUESTS))).hasSize(1);
    }

    @Test
    void checkInInThePastIsRejected() {
        assertRejected(DomainValidationException.class, "CHECK_IN_IN_PAST",
                () -> search(DateRange.of(clock.today().minusDays(1), clock.today().plusDays(1))));
    }

    private List<PropertySearchResult> search(DateRange stay) {
        return search.search(SearchCriteria.of(city, stay, TWO_GUESTS));
    }

    private List<PropertySearchResult> search(SearchCriteria criteria) {
        return search.search(criteria);
    }

    private SearchCriteria criteria(DateRange stay, String locality, Money min, Money max, Set<Amenity> amenities,
                                    Integer minStars) {
        return new SearchCriteria(city, stay, TWO_GUESTS, locality, min, max, amenities, minStars);
    }

    private Property onboard(String name, String locality, int stars, Set<Amenity> amenities, RoomTypeDraft... rooms) {
        PropertyDraft draft = new PropertyDraft(name, new Location("1 Main Road", locality, city), StarRating.of(stars),
                amenities, List.of(rooms), CancellationPolicyType.FLEXIBLE);
        return onboarding.onboardOwner(TestData.ownerWith(draft)).properties().get(0);
    }

    private static RoomTypeDraft room(String name, int totalRooms, long pricePerNight) {
        return TestData.roomType(name, 2, totalRooms, pricePerNight);
    }

    private static List<String> names(List<PropertySearchResult> results) {
        return results.stream().map(result -> result.property().name()).toList();
    }

    private static List<String> roomTypesOffered(List<PropertySearchResult> results) {
        return results.stream().flatMap(result -> result.offers().stream())
                .map(offer -> offer.roomType().name()).toList();
    }
}
