package com.hotelbooking.property.domain;

import com.hotelbooking.common.domain.Money;
import com.hotelbooking.common.domain.Occupancy;
import com.hotelbooking.common.domain.error.DomainValidationException;
import com.hotelbooking.common.domain.error.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static com.hotelbooking.support.DomainAssertions.assertRejected;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PropertyTest {

    private static final Location LOCATION = new Location("1 MG Road", "Indiranagar", "Bengaluru");

    @Test
    void propertyNeedsAtLeastOneRoomType() {
        assertThatThrownBy(() -> property(List.of())).isInstanceOf(DomainValidationException.class);
    }

    @Test
    void roomTypeNamesMustBeUniqueIgnoringCase() {
        List<RoomType> roomTypes = List.of(deluxe(), RoomType.create("DELUXE", 3, 2, Money.inr(5000)));

        assertRejected(DomainValidationException.class, "DUPLICATE_ROOM_TYPE", () -> property(roomTypes));
    }

    @Test
    void looksUpItsOwnRoomTypesOnly() {
        RoomType deluxe = deluxe();
        Property property = property(List.of(deluxe));

        assertThat(property.roomType(deluxe.id())).isEqualTo(deluxe);
        assertThatThrownBy(() -> property.roomType(RoomTypeId.newId())).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void roomsTogetherMustFitAllGuests() {
        RoomType twoPerRoom = deluxe();

        assertThat(twoPerRoom.canAccommodate(Occupancy.of(4, 2))).isTrue();
        assertThat(twoPerRoom.canAccommodate(Occupancy.of(5, 2))).isFalse();
        assertRejected(DomainValidationException.class, "OCCUPANCY_EXCEEDED",
                () -> twoPerRoom.ensureCanAccommodate(Occupancy.of(3, 1)));
    }

    @Test
    void offersAllChecksEveryRequestedAmenity() {
        Property property = property(List.of(deluxe()));

        assertThat(property.offersAll(Set.of(Amenity.WIFI))).isTrue();
        assertThat(property.offersAll(Set.of(Amenity.WIFI, Amenity.SPA))).isFalse();
    }

    private static RoomType deluxe() {
        return RoomType.create("Deluxe", 2, 5, Money.inr(4000));
    }

    private static Property property(List<RoomType> roomTypes) {
        return Property.onboard(OwnerId.newId(), "Lemon Leaf", LOCATION, StarRating.of(4),
                Set.of(Amenity.WIFI, Amenity.PARKING), roomTypes, CancellationPolicyType.FLEXIBLE);
    }
}
