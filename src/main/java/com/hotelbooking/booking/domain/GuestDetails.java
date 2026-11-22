package com.hotelbooking.booking.domain;

import com.hotelbooking.common.domain.ContactDetails;
import com.hotelbooking.common.domain.Guard;

public record GuestDetails(String fullName, ContactDetails contact) {

    public GuestDetails {
        fullName = Guard.notBlank(fullName, "guest fullName");
        Guard.notNull(contact, "guest contact");
    }
}
