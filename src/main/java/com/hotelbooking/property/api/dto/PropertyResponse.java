package com.hotelbooking.property.api.dto;

import com.hotelbooking.common.api.MoneyDto;
import com.hotelbooking.property.domain.Amenity;
import com.hotelbooking.property.domain.CancellationPolicyType;
import com.hotelbooking.property.domain.Property;
import com.hotelbooking.property.domain.RoomType;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record PropertyResponse(
        UUID propertyId,
        UUID ownerId,
        String name,
        String addressLine,
        String locality,
        String city,
        int starRating,
        Set<Amenity> amenities,
        CancellationPolicyType cancellationPolicy,
        List<RoomTypeResponse> roomTypes) {

    public static PropertyResponse from(Property property) {
        return new PropertyResponse(
                property.id().value(),
                property.ownerId().value(),
                property.name(),
                property.location().addressLine(),
                property.location().locality(),
                property.location().city(),
                property.starRating().stars(),
                property.amenities(),
                property.cancellationPolicy(),
                property.roomTypes().stream().map(RoomTypeResponse::from).toList());
    }

    public record RoomTypeResponse(UUID roomTypeId, String name, int maxOccupancy, int totalRooms,
                                   MoneyDto basePricePerNight) {

        static RoomTypeResponse from(RoomType roomType) {
            return new RoomTypeResponse(roomType.id().value(), roomType.name(), roomType.maxOccupancy(),
                    roomType.totalRooms(), MoneyDto.from(roomType.basePricePerNight()));
        }
    }
}
