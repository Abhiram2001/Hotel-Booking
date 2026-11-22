package com.hotelbooking.search.api;

import com.hotelbooking.common.api.MoneyDto;
import com.hotelbooking.property.domain.Amenity;
import com.hotelbooking.property.domain.CancellationPolicyType;
import com.hotelbooking.property.domain.Property;
import com.hotelbooking.search.application.PropertySearchResult;
import com.hotelbooking.search.application.RoomOffer;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Wrapped in an object (not a bare array) so paging or facets can be added without breaking clients. */
record SearchResponse(int count, List<PropertyMatch> results) {

    static SearchResponse from(List<PropertySearchResult> results) {
        return new SearchResponse(results.size(), results.stream().map(PropertyMatch::from).toList());
    }

    record PropertyMatch(UUID propertyId, String name, String locality, String city, int starRating,
                         Set<Amenity> amenities, CancellationPolicyType cancellationPolicy, List<Offer> offers) {

        static PropertyMatch from(PropertySearchResult result) {
            Property property = result.property();
            return new PropertyMatch(property.id().value(), property.name(), property.location().locality(),
                    property.location().city(), property.starRating().stars(), property.amenities(),
                    property.cancellationPolicy(), result.offers().stream().map(Offer::from).toList());
        }
    }

    record Offer(UUID roomTypeId, String roomType, int maxOccupancy, int availableRooms,
                 MoneyDto averageNightlyRate, MoneyDto totalPrice) {

        static Offer from(RoomOffer offer) {
            return new Offer(offer.roomType().id().value(), offer.roomType().name(), offer.roomType().maxOccupancy(),
                    offer.availableRooms(), MoneyDto.from(offer.quote().averageNightlyRate()),
                    MoneyDto.from(offer.quote().total()));
        }
    }
}
