package com.hotelbooking.property.infrastructure.persistence;

import com.hotelbooking.property.domain.OwnerId;
import com.hotelbooking.property.domain.Property;
import com.hotelbooking.property.domain.PropertyId;
import com.hotelbooking.property.domain.PropertyRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Properties are immutable after onboarding in this scope, so save only inserts. */
@Repository
class PropertyRepositoryAdapter implements PropertyRepository {

    private final PropertyJpaRepository jpa;

    PropertyRepositoryAdapter(PropertyJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Property save(Property property) {
        jpa.save(PropertyPersistenceMapper.toEntity(property));
        return property;
    }

    @Override
    public Optional<Property> findById(PropertyId id) {
        return jpa.findById(id.value()).map(PropertyPersistenceMapper::toDomain);
    }

    @Override
    public List<Property> findByOwner(OwnerId ownerId) {
        return jpa.findByOwnerId(ownerId.value()).stream().map(PropertyPersistenceMapper::toDomain).toList();
    }

    @Override
    public List<Property> findByCity(String city) {
        return jpa.findByCityKey(PropertyPersistenceMapper.cityKey(city)).stream()
                .map(PropertyPersistenceMapper::toDomain)
                .toList();
    }
}
