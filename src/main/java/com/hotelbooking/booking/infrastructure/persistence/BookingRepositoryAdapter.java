package com.hotelbooking.booking.infrastructure.persistence;

import com.hotelbooking.booking.domain.Booking;
import com.hotelbooking.booking.domain.BookingId;
import com.hotelbooking.booking.domain.BookingRepository;
import com.hotelbooking.booking.domain.BookingStatus;
import com.hotelbooking.booking.domain.GuestDetails;
import com.hotelbooking.booking.domain.Stay;
import com.hotelbooking.common.domain.ContactDetails;
import com.hotelbooking.common.domain.DateRange;
import com.hotelbooking.common.domain.Money;
import com.hotelbooking.common.domain.Occupancy;
import com.hotelbooking.property.domain.PropertyId;
import com.hotelbooking.property.domain.RoomTypeId;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

@Repository
class BookingRepositoryAdapter implements BookingRepository {

    private final BookingJpaRepository jpa;

    BookingRepositoryAdapter(BookingJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Booking save(Booking booking) {
        BookingEntity entity = jpa.findById(booking.id().value()).orElseGet(() -> toNewEntity(booking));
        entity.updateLifecycle(booking.status(), booking.confirmedAt(), booking.cancelledAt());
        jpa.save(entity);
        return booking;
    }

    @Override
    public Optional<Booking> findById(BookingId id) {
        return jpa.findById(id.value()).map(BookingRepositoryAdapter::toDomain);
    }

    @Override
    public Optional<Booking> findByIdForUpdate(BookingId id) {
        return jpa.findByIdForUpdate(id.value()).map(BookingRepositoryAdapter::toDomain);
    }

    @Override
    public List<BookingId> findExpiredHolds(Instant now, int limit) {
        return jpa.findIdsWithExpiredHold(BookingStatus.PENDING_PAYMENT, now, PageRequest.of(0, limit)).stream()
                .map(BookingId::new)
                .toList();
    }

    private static BookingEntity toNewEntity(Booking booking) {
        Stay stay = booking.stay();
        GuestDetails guest = booking.guest();
        return new BookingEntity(
                booking.id().value(),
                stay.propertyId().value(),
                stay.roomTypeId().value(),
                stay.dates().checkIn(),
                stay.dates().checkOut(),
                stay.occupancy().guests(),
                stay.occupancy().rooms(),
                guest.fullName(),
                guest.contact().email(),
                guest.contact().phone(),
                booking.totalAmount().amount(),
                booking.totalAmount().currency().getCurrencyCode(),
                booking.cancellationPolicy(),
                booking.createdAt(),
                booking.holdExpiresAt());
    }

    private static Booking toDomain(BookingEntity entity) {
        Stay stay = new Stay(
                new PropertyId(entity.getPropertyId()),
                new RoomTypeId(entity.getRoomTypeId()),
                DateRange.of(entity.getCheckIn(), entity.getCheckOut()),
                Occupancy.of(entity.getGuests(), entity.getRooms()));
        GuestDetails guest = new GuestDetails(entity.getGuestName(),
                new ContactDetails(entity.getGuestEmail(), entity.getGuestPhone()));
        return Booking.restore(
                new BookingId(entity.getId()),
                stay,
                guest,
                Money.of(entity.getTotalAmount(), Currency.getInstance(entity.getCurrency())),
                entity.getCancellationPolicy(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getHoldExpiresAt(),
                entity.getConfirmedAt(),
                entity.getCancelledAt());
    }
}
