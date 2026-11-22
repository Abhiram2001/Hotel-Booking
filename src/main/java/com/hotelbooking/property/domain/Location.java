package com.hotelbooking.property.domain;

import com.hotelbooking.common.domain.Guard;

public record Location(String addressLine, String locality, String city) {

    public Location {
        addressLine = Guard.notBlank(addressLine, "addressLine");
        locality = Guard.notBlank(locality, "locality");
        city = Guard.notBlank(city, "city");
    }

    public boolean isInLocality(String candidate) {
        return candidate != null && locality.equalsIgnoreCase(candidate.trim());
    }
}
