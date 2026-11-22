package com.hotelbooking.property.application;

import com.hotelbooking.common.domain.ContactDetails;
import com.hotelbooking.common.domain.Guard;

import java.util.List;

/** Onboards an owner with one property (standalone hotel) or many (chain). There is no separate path for either. */
public record OnboardOwnerCommand(String name, ContactDetails contact, List<PropertyDraft> properties) {

    public OnboardOwnerCommand {
        Guard.notNull(properties, "properties");
        Guard.isTrue(!properties.isEmpty(), "An owner must onboard at least one property");
        properties = List.copyOf(properties);
    }
}
