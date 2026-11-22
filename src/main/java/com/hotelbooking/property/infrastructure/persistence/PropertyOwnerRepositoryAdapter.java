package com.hotelbooking.property.infrastructure.persistence;

import com.hotelbooking.property.domain.OwnerId;
import com.hotelbooking.property.domain.PropertyOwner;
import com.hotelbooking.property.domain.PropertyOwnerRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class PropertyOwnerRepositoryAdapter implements PropertyOwnerRepository {

    private final PropertyOwnerJpaRepository jpa;

    PropertyOwnerRepositoryAdapter(PropertyOwnerJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public PropertyOwner save(PropertyOwner owner) {
        jpa.save(PropertyPersistenceMapper.toEntity(owner));
        return owner;
    }

    @Override
    public Optional<PropertyOwner> findById(OwnerId id) {
        return jpa.findById(id.value()).map(PropertyPersistenceMapper::toDomain);
    }
}
