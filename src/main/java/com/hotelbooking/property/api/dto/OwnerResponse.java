package com.hotelbooking.property.api.dto;

import com.hotelbooking.property.application.OwnerPortfolio;
import com.hotelbooking.property.domain.PropertyOwner;

import java.util.List;
import java.util.UUID;

public record OwnerResponse(UUID ownerId, String name, String email, String phone, List<PropertyResponse> properties) {

    public static OwnerResponse from(OwnerPortfolio portfolio) {
        PropertyOwner owner = portfolio.owner();
        return new OwnerResponse(owner.id().value(), owner.name(), owner.contact().email(), owner.contact().phone(),
                portfolio.properties().stream().map(PropertyResponse::from).toList());
    }
}
