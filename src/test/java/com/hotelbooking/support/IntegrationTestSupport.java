package com.hotelbooking.support;

import com.hotelbooking.booking.application.BookingService;
import com.hotelbooking.booking.domain.Booking;
import com.hotelbooking.common.domain.DateRange;
import com.hotelbooking.common.domain.Occupancy;
import com.hotelbooking.inventory.application.InventoryService;
import com.hotelbooking.payment.application.PayForBookingCommand;
import com.hotelbooking.payment.application.PaymentResult;
import com.hotelbooking.payment.application.PaymentService;
import com.hotelbooking.payment.domain.IdempotencyKey;
import com.hotelbooking.payment.domain.PaymentInstrument;
import com.hotelbooking.payment.domain.UpiInstrument;
import com.hotelbooking.property.application.PropertyOnboardingService;
import com.hotelbooking.property.domain.CancellationPolicyType;
import com.hotelbooking.property.domain.Property;
import com.hotelbooking.property.domain.RoomTypeId;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.UUID;

/** Shared wiring and helpers for service-level integration tests. */
@IntegrationTest
public abstract class IntegrationTestSupport {

    protected static final long PRICE_PER_NIGHT = 4000;

    @Autowired
    protected PropertyOnboardingService onboarding;
    @Autowired
    protected BookingService bookings;
    @Autowired
    protected PaymentService payments;
    @Autowired
    protected InventoryService inventory;
    @Autowired
    protected MutableClock clock;
    @Autowired
    protected ScriptablePaymentGateway gateway;

    @BeforeEach
    protected final void resetTestDoubles() {
        clock.reset();
        gateway.reset();
    }

    /** A property with one room type ("Deluxe", 2 guests per room) in a city no other test uses. */
    protected Property onboardProperty(CancellationPolicyType policy, int rooms) {
        return onboarding.onboardOwner(TestData.ownerWith(TestData.property(TestData.uniqueCity(), policy,
                TestData.roomType("Deluxe", 2, rooms, PRICE_PER_NIGHT)))).properties().get(0);
    }

    /** Nights from {@code fromDay} to {@code toDay} days after today (check-out day excluded). */
    protected DateRange days(int fromDay, int toDay) {
        return DateRange.of(clock.today().plusDays(fromDay), clock.today().plusDays(toDay));
    }

    protected Booking book(Property property, DateRange stay) {
        return bookings.create(TestData.booking(property, stay, Occupancy.of(1, 1)));
    }

    protected PaymentResult pay(Booking booking, PaymentInstrument instrument) {
        return payments.pay(new PayForBookingCommand(booking.id(), instrument, newKey()));
    }

    protected PaymentResult payByUpi(Booking booking) {
        return pay(booking, new UpiInstrument("asha@okicici"));
    }

    protected int availableRooms(Property property, DateRange stay) {
        RoomTypeId roomTypeId = property.roomTypes().get(0).id();
        return inventory.availableRooms(Set.of(roomTypeId), stay).get(roomTypeId);
    }

    protected static IdempotencyKey newKey() {
        return new IdempotencyKey(UUID.randomUUID().toString());
    }
}
