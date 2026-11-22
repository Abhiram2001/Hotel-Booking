package com.hotelbooking.property.domain;

import com.hotelbooking.common.domain.ContactDetails;
import com.hotelbooking.common.domain.Guard;

import java.util.Objects;

/**
 * The account that owns and manages properties. A hotel chain is an owner with many properties; a standalone
 * hotel is an owner with one. Both go through the same model and the same onboarding flow.
 */
public final class PropertyOwner {

    private final OwnerId id;
    private final String name;
    private final ContactDetails contact;

    private PropertyOwner(OwnerId id, String name, ContactDetails contact) {
        this.id = Guard.notNull(id, "ownerId");
        this.name = Guard.notBlank(name, "owner name");
        this.contact = Guard.notNull(contact, "contact");
    }

    public static PropertyOwner register(String name, ContactDetails contact) {
        return new PropertyOwner(OwnerId.newId(), name, contact);
    }

    public static PropertyOwner restore(OwnerId id, String name, ContactDetails contact) {
        return new PropertyOwner(id, name, contact);
    }

    public OwnerId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public ContactDetails contact() {
        return contact;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof PropertyOwner that && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
