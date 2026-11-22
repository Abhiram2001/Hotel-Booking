package com.hotelbooking.property.infrastructure.persistence;

import com.hotelbooking.common.infrastructure.persistence.AssignedIdEntity;
import com.hotelbooking.property.domain.Amenity;
import com.hotelbooking.property.domain.CancellationPolicyType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "property", indexes = {
        @Index(name = "idx_property_city_key", columnList = "city_key"),
        @Index(name = "idx_property_owner", columnList = "owner_id")
})
class PropertyEntity extends AssignedIdEntity<UUID> {

    @Id
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(nullable = false)
    private String name;

    @Column(name = "address_line", nullable = false)
    private String addressLine;

    @Column(nullable = false)
    private String locality;

    @Column(nullable = false)
    private String city;

    /** Lower-cased city, so city search can use a plain index. */
    @Column(name = "city_key", nullable = false)
    private String cityKey;

    @Column(name = "star_rating", nullable = false)
    private int starRating;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancellation_policy", nullable = false)
    private CancellationPolicyType cancellationPolicy;

    @ElementCollection
    @CollectionTable(name = "property_amenity", joinColumns = @JoinColumn(name = "property_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "amenity", nullable = false)
    private Set<Amenity> amenities = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "property_id", nullable = false)
    @OrderBy("name")
    private List<RoomTypeEntity> roomTypes = new ArrayList<>();

    protected PropertyEntity() {
    }

    PropertyEntity(UUID id, UUID ownerId, String name, String addressLine, String locality, String city, String cityKey,
                   int starRating, CancellationPolicyType cancellationPolicy, Set<Amenity> amenities,
                   List<RoomTypeEntity> roomTypes) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.addressLine = addressLine;
        this.locality = locality;
        this.city = city;
        this.cityKey = cityKey;
        this.starRating = starRating;
        this.cancellationPolicy = cancellationPolicy;
        this.amenities = new HashSet<>(amenities);
        this.roomTypes = new ArrayList<>(roomTypes);
    }

    @Override
    public UUID getId() {
        return id;
    }

    UUID getOwnerId() {
        return ownerId;
    }

    String getName() {
        return name;
    }

    String getAddressLine() {
        return addressLine;
    }

    String getLocality() {
        return locality;
    }

    String getCity() {
        return city;
    }

    int getStarRating() {
        return starRating;
    }

    CancellationPolicyType getCancellationPolicy() {
        return cancellationPolicy;
    }

    Set<Amenity> getAmenities() {
        return amenities;
    }

    List<RoomTypeEntity> getRoomTypes() {
        return roomTypes;
    }
}
