package com.hotelbooking.property.application;

import com.hotelbooking.property.domain.Property;
import com.hotelbooking.property.domain.PropertyOwner;

import java.util.List;

public record OwnerPortfolio(PropertyOwner owner, List<Property> properties) {
}
