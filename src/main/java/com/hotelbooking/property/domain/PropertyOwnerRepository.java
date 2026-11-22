package com.hotelbooking.property.domain;

import java.util.Optional;

public interface PropertyOwnerRepository {

    PropertyOwner save(PropertyOwner owner);

    Optional<PropertyOwner> findById(OwnerId id);
}
