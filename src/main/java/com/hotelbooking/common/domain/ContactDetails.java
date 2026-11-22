package com.hotelbooking.common.domain;

/** Email and phone of an owner or a guest. Format checks happen at the API boundary. */
public record ContactDetails(String email, String phone) {

    public ContactDetails {
        email = Guard.notBlank(email, "email");
        phone = Guard.notBlank(phone, "phone");
    }
}
