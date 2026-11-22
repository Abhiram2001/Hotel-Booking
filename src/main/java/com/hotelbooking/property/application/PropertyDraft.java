package com.hotelbooking.property.application;

import com.hotelbooking.common.domain.Guard;
import com.hotelbooking.property.domain.Amenity;
import com.hotelbooking.property.domain.CancellationPolicyType;
import com.hotelbooking.property.domain.Location;
import com.hotelbooking.property.domain.OwnerId;
import com.hotelbooking.property.domain.Property;
import com.hotelbooking.property.domain.StarRating;

import java.util.List;
import java.util.Set;

/** Everything needed to onboard one property, before it has an id or an owner. */
public record PropertyDraft(String name, Location location, StarRating starRating, Set<Amenity> amenities,
                            List<RoomTypeDraft> roomTypes, CancellationPolicyType cancellationPolicy) {

    public PropertyDraft {
        Guard.notNull(roomTypes, "roomTypes");
    }

    Property toProperty(OwnerId ownerId) {
        return Property.onboard(ownerId, name, location, starRating, amenities,
                roomTypes.stream().map(RoomTypeDraft::toRoomType).toList(), cancellationPolicy);
    }
}
