package com.hotelbooking.property.infrastructure.persistence;

import com.hotelbooking.common.domain.ContactDetails;
import com.hotelbooking.common.domain.Money;
import com.hotelbooking.property.domain.Location;
import com.hotelbooking.property.domain.OwnerId;
import com.hotelbooking.property.domain.Property;
import com.hotelbooking.property.domain.PropertyId;
import com.hotelbooking.property.domain.PropertyOwner;
import com.hotelbooking.property.domain.RoomType;
import com.hotelbooking.property.domain.RoomTypeId;
import com.hotelbooking.property.domain.StarRating;

import java.util.Currency;
import java.util.Locale;

/** Translates between domain objects and JPA entities so neither side leaks into the other. */
final class PropertyPersistenceMapper {

    private PropertyPersistenceMapper() {
    }

    static String cityKey(String city) {
        return city.trim().toLowerCase(Locale.ROOT);
    }

    static PropertyOwnerEntity toEntity(PropertyOwner owner) {
        return new PropertyOwnerEntity(owner.id().value(), owner.name(), owner.contact().email(), owner.contact().phone());
    }

    static PropertyOwner toDomain(PropertyOwnerEntity entity) {
        return PropertyOwner.restore(new OwnerId(entity.getId()), entity.getName(),
                new ContactDetails(entity.getEmail(), entity.getPhone()));
    }

    static PropertyEntity toEntity(Property property) {
        Location location = property.location();
        return new PropertyEntity(
                property.id().value(),
                property.ownerId().value(),
                property.name(),
                location.addressLine(),
                location.locality(),
                location.city(),
                cityKey(location.city()),
                property.starRating().stars(),
                property.cancellationPolicy(),
                property.amenities(),
                property.roomTypes().stream().map(PropertyPersistenceMapper::toEntity).toList());
    }

    static Property toDomain(PropertyEntity entity) {
        return Property.restore(
                new PropertyId(entity.getId()),
                new OwnerId(entity.getOwnerId()),
                entity.getName(),
                new Location(entity.getAddressLine(), entity.getLocality(), entity.getCity()),
                StarRating.of(entity.getStarRating()),
                entity.getAmenities(),
                entity.getRoomTypes().stream().map(PropertyPersistenceMapper::toDomain).toList(),
                entity.getCancellationPolicy());
    }

    private static RoomTypeEntity toEntity(RoomType roomType) {
        Money price = roomType.basePricePerNight();
        return new RoomTypeEntity(roomType.id().value(), roomType.name(), roomType.maxOccupancy(),
                roomType.totalRooms(), price.amount(), price.currency().getCurrencyCode());
    }

    private static RoomType toDomain(RoomTypeEntity entity) {
        return RoomType.restore(new RoomTypeId(entity.getId()), entity.getName(), entity.getMaxOccupancy(),
                entity.getTotalRooms(), Money.of(entity.getBasePrice(), Currency.getInstance(entity.getCurrency())));
    }
}
