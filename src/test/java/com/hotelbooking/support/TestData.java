package com.hotelbooking.support;

import com.hotelbooking.booking.application.CreateBookingCommand;
import com.hotelbooking.booking.domain.GuestDetails;
import com.hotelbooking.common.domain.ContactDetails;
import com.hotelbooking.common.domain.DateRange;
import com.hotelbooking.common.domain.Money;
import com.hotelbooking.common.domain.Occupancy;
import com.hotelbooking.property.application.OnboardOwnerCommand;
import com.hotelbooking.property.application.PropertyDraft;
import com.hotelbooking.property.application.RoomTypeDraft;
import com.hotelbooking.property.domain.Amenity;
import com.hotelbooking.property.domain.CancellationPolicyType;
import com.hotelbooking.property.domain.Location;
import com.hotelbooking.property.domain.Property;
import com.hotelbooking.property.domain.StarRating;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class TestData {

    private TestData() {
    }

    /** A city name no other test uses, so searches only see this test's properties. */
    public static String uniqueCity() {
        return "City-" + UUID.randomUUID().toString().substring(0, 8);
    }

    public static ContactDetails contact() {
        return new ContactDetails("owner@example.com", "+919876543210");
    }

    public static GuestDetails guest() {
        return new GuestDetails("Asha Rao", new ContactDetails("asha@example.com", "+919812345678"));
    }

    public static PropertyDraft property(String city, CancellationPolicyType policy, RoomTypeDraft... roomTypes) {
        return new PropertyDraft("Hotel " + city, new Location("1 MG Road", "Central", city), StarRating.of(4),
                Set.of(Amenity.WIFI, Amenity.PARKING), List.of(roomTypes), policy);
    }

    public static RoomTypeDraft roomType(String name, int maxOccupancy, int totalRooms, long pricePerNight) {
        return new RoomTypeDraft(name, maxOccupancy, totalRooms, Money.inr(pricePerNight));
    }

    public static OnboardOwnerCommand ownerWith(PropertyDraft... properties) {
        return new OnboardOwnerCommand("Test Owner", contact(), List.of(properties));
    }

    public static CreateBookingCommand booking(Property property, DateRange dates, Occupancy occupancy) {
        return new CreateBookingCommand(property.id(), property.roomTypes().get(0).id(), dates, occupancy, guest());
    }
}
