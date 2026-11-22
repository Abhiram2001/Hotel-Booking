package com.hotelbooking.property.domain;

import java.util.List;
import java.util.Optional;

public interface PropertyRepository {

    Property save(Property property);

    Optional<Property> findById(PropertyId id);

    List<Property> findByOwner(OwnerId ownerId);

    /** Case-insensitive match on city name. */
    List<Property> findByCity(String city);
}
