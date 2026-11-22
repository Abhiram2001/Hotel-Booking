package com.hotelbooking.property.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface PropertyJpaRepository extends JpaRepository<PropertyEntity, UUID> {

    List<PropertyEntity> findByOwnerId(UUID ownerId);

    List<PropertyEntity> findByCityKey(String cityKey);
}
