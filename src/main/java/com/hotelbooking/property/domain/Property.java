package com.hotelbooking.property.domain;

import com.hotelbooking.common.domain.Guard;
import com.hotelbooking.common.domain.error.DomainValidationException;
import com.hotelbooking.common.domain.error.ResourceNotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** A bookable property and its room types. Every property belongs to exactly one {@link PropertyOwner}. */
public final class Property {

    private final PropertyId id;
    private final OwnerId ownerId;
    private final String name;
    private final Location location;
    private final StarRating starRating;
    private final Set<Amenity> amenities;
    private final List<RoomType> roomTypes;
    private final CancellationPolicyType cancellationPolicy;

    private Property(PropertyId id, OwnerId ownerId, String name, Location location, StarRating starRating,
                     Set<Amenity> amenities, List<RoomType> roomTypes, CancellationPolicyType cancellationPolicy) {
        this.id = Guard.notNull(id, "propertyId");
        this.ownerId = Guard.notNull(ownerId, "ownerId");
        this.name = Guard.notBlank(name, "property name");
        this.location = Guard.notNull(location, "location");
        this.starRating = Guard.notNull(starRating, "starRating");
        this.amenities = amenities == null ? Set.of() : Set.copyOf(amenities);
        this.roomTypes = List.copyOf(Guard.notNull(roomTypes, "roomTypes"));
        this.cancellationPolicy = Guard.notNull(cancellationPolicy, "cancellationPolicy");
        Guard.isTrue(!this.roomTypes.isEmpty(), "A property needs at least one room type");
        ensureUniqueRoomTypeNames(this.roomTypes);
    }

    public static Property onboard(OwnerId ownerId, String name, Location location, StarRating starRating,
                                   Set<Amenity> amenities, List<RoomType> roomTypes,
                                   CancellationPolicyType cancellationPolicy) {
        return new Property(PropertyId.newId(), ownerId, name, location, starRating, amenities, roomTypes, cancellationPolicy);
    }

    public static Property restore(PropertyId id, OwnerId ownerId, String name, Location location, StarRating starRating,
                                   Set<Amenity> amenities, List<RoomType> roomTypes,
                                   CancellationPolicyType cancellationPolicy) {
        return new Property(id, ownerId, name, location, starRating, amenities, roomTypes, cancellationPolicy);
    }

    public RoomType roomType(RoomTypeId roomTypeId) {
        return findRoomType(roomTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Room type", roomTypeId + " in property " + id));
    }

    public Optional<RoomType> findRoomType(RoomTypeId roomTypeId) {
        return roomTypes.stream().filter(roomType -> roomType.id().equals(roomTypeId)).findFirst();
    }

    public boolean offersAll(Set<Amenity> required) {
        return amenities.containsAll(required);
    }

    private static void ensureUniqueRoomTypeNames(List<RoomType> roomTypes) {
        Set<String> seen = new HashSet<>();
        for (RoomType roomType : roomTypes) {
            if (!seen.add(roomType.name().toLowerCase(Locale.ROOT))) {
                throw new DomainValidationException("DUPLICATE_ROOM_TYPE",
                        "Room type '%s' is listed more than once".formatted(roomType.name()));
            }
        }
    }

    public PropertyId id() {
        return id;
    }

    public OwnerId ownerId() {
        return ownerId;
    }

    public String name() {
        return name;
    }

    public Location location() {
        return location;
    }

    public StarRating starRating() {
        return starRating;
    }

    public Set<Amenity> amenities() {
        return amenities;
    }

    public List<RoomType> roomTypes() {
        return roomTypes;
    }

    public CancellationPolicyType cancellationPolicy() {
        return cancellationPolicy;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Property that && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
