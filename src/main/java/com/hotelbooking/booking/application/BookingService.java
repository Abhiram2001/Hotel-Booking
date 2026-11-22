package com.hotelbooking.booking.application;

import com.hotelbooking.booking.domain.Booking;
import com.hotelbooking.booking.domain.BookingCancellation;
import com.hotelbooking.booking.domain.BookingId;
import com.hotelbooking.booking.domain.BookingRepository;
import com.hotelbooking.booking.domain.Stay;
import com.hotelbooking.common.domain.error.DomainValidationException;
import com.hotelbooking.common.domain.error.ResourceNotFoundException;
import com.hotelbooking.inventory.application.InventoryService;
import com.hotelbooking.pricing.domain.PriceQuote;
import com.hotelbooking.pricing.domain.PricingRequest;
import com.hotelbooking.pricing.domain.PricingStrategy;
import com.hotelbooking.property.application.PropertyQueryService;
import com.hotelbooking.property.domain.Property;
import com.hotelbooking.property.domain.RoomType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Owns every booking state change and its inventory side effect. Each change locks the booking row first,
 * then the inventory rows, so lock order is the same everywhere.
 */
@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookings;
    private final PropertyQueryService properties;
    private final InventoryService inventory;
    private final PricingStrategy pricing;
    private final BookingProperties config;
    private final Clock clock;

    public BookingService(BookingRepository bookings, PropertyQueryService properties, InventoryService inventory,
                          PricingStrategy pricing, BookingProperties config, Clock clock) {
        this.bookings = bookings;
        this.properties = properties;
        this.inventory = inventory;
        this.pricing = pricing;
        this.config = config;
        this.clock = clock;
    }

    @Transactional
    public Booking create(CreateBookingCommand command) {
        validateStayLimits(command);
        Property property = properties.getProperty(command.propertyId());
        RoomType roomType = property.roomType(command.roomTypeId());
        roomType.ensureCanAccommodate(command.occupancy());

        int rooms = command.occupancy().rooms();
        PriceQuote quote = pricing.quote(new PricingRequest(property, roomType, command.dates(), rooms));
        inventory.reserve(roomType.id(), command.dates(), rooms);

        Stay stay = new Stay(property.id(), roomType.id(), command.dates(), command.occupancy());
        Booking booking = Booking.place(stay, command.guest(), quote.total(), property.cancellationPolicy(),
                clock.instant(), config.holdDuration());
        bookings.save(booking);
        log.info("Booking {} placed: property {}, room type {}, {} to {}, {} room(s), total {}, held until {}",
                booking.id(), property.id(), roomType.id(), command.dates().checkIn(), command.dates().checkOut(),
                rooms, booking.totalAmount(), booking.holdExpiresAt());
        return booking;
    }

    @Transactional(readOnly = true)
    public Booking get(BookingId id) {
        return bookings.findById(id).orElseThrow(() -> notFound(id));
    }

    /** For other modules that must change a booking together with their own data in one transaction. */
    @Transactional(propagation = Propagation.MANDATORY)
    public Booking getForUpdate(BookingId id) {
        return lock(id);
    }

    @Transactional
    public Booking confirm(BookingId id) {
        Booking booking = lock(id);
        booking.confirm(clock.instant());
        bookings.save(booking);
        log.info("Booking {} confirmed", id);
        return booking;
    }

    @Transactional
    public BookingCancellation cancel(BookingId id) {
        Booking booking = lock(id);
        BookingCancellation cancellation = booking.cancel(clock.instant(), checkInInstant(booking));
        releaseInventory(booking);
        bookings.save(booking);
        log.info("Booking {} cancelled from {}, {} hour(s) before check-in; {} room(s) released", id,
                cancellation.previousStatus(), cancellation.noticeBeforeCheckIn().toHours(), booking.stay().occupancy().rooms());
        return cancellation;
    }

    /** @return false when the booking was paid or cancelled before the expiry got to it */
    @Transactional
    public boolean expireHold(BookingId id) {
        Booking booking = lock(id);
        Instant now = clock.instant();
        if (!booking.isHoldExpired(now)) {
            log.debug("Booking {} is {} and no longer needs to expire", id, booking.status());
            return false;
        }
        booking.expireHold(now);
        releaseInventory(booking);
        bookings.save(booking);
        log.info("Booking {} expired unpaid; {} room(s) released", id, booking.stay().occupancy().rooms());
        return true;
    }

    @Transactional(readOnly = true)
    public List<BookingId> findExpiredHolds(int limit) {
        return bookings.findExpiredHolds(clock.instant(), limit);
    }

    private void validateStayLimits(CreateBookingCommand command) {
        command.dates().ensureNotInPast(LocalDate.now(clock));
        if (command.dates().nights() > config.maxNights()) {
            throw new DomainValidationException("STAY_TOO_LONG",
                    "A booking can cover at most %d nights".formatted(config.maxNights()));
        }
        if (command.occupancy().rooms() > config.maxRoomsPerBooking()) {
            throw new DomainValidationException("TOO_MANY_ROOMS",
                    "A booking can include at most %d rooms".formatted(config.maxRoomsPerBooking()));
        }
    }

    private Instant checkInInstant(Booking booking) {
        return booking.stay().dates().checkIn().atTime(config.checkInTime()).atZone(clock.getZone()).toInstant();
    }

    private void releaseInventory(Booking booking) {
        Stay stay = booking.stay();
        inventory.release(stay.roomTypeId(), stay.dates(), stay.occupancy().rooms());
    }

    private Booking lock(BookingId id) {
        return bookings.findByIdForUpdate(id).orElseThrow(() -> notFound(id));
    }

    private static ResourceNotFoundException notFound(BookingId id) {
        return new ResourceNotFoundException("Booking", id);
    }
}
