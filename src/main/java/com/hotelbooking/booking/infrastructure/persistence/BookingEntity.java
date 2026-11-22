package com.hotelbooking.booking.infrastructure.persistence;

import com.hotelbooking.booking.domain.BookingStatus;
import com.hotelbooking.common.infrastructure.persistence.AssignedIdEntity;
import com.hotelbooking.property.domain.CancellationPolicyType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "booking", indexes = @Index(name = "idx_booking_status_hold", columnList = "status, hold_expires_at"))
class BookingEntity extends AssignedIdEntity<UUID> {

    @Id
    private UUID id;

    @Column(name = "property_id", nullable = false)
    private UUID propertyId;

    @Column(name = "room_type_id", nullable = false)
    private UUID roomTypeId;

    @Column(name = "check_in", nullable = false)
    private LocalDate checkIn;

    @Column(name = "check_out", nullable = false)
    private LocalDate checkOut;

    @Column(nullable = false)
    private int guests;

    @Column(nullable = false)
    private int rooms;

    @Column(name = "guest_name", nullable = false)
    private String guestName;

    @Column(name = "guest_email", nullable = false)
    private String guestEmail;

    @Column(name = "guest_phone", nullable = false)
    private String guestPhone;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancellation_policy", nullable = false)
    private CancellationPolicyType cancellationPolicy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "hold_expires_at", nullable = false)
    private Instant holdExpiresAt;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    protected BookingEntity() {
    }

    BookingEntity(UUID id, UUID propertyId, UUID roomTypeId, LocalDate checkIn, LocalDate checkOut, int guests,
                  int rooms, String guestName, String guestEmail, String guestPhone, BigDecimal totalAmount,
                  String currency, CancellationPolicyType cancellationPolicy, Instant createdAt, Instant holdExpiresAt) {
        this.id = id;
        this.propertyId = propertyId;
        this.roomTypeId = roomTypeId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.guests = guests;
        this.rooms = rooms;
        this.guestName = guestName;
        this.guestEmail = guestEmail;
        this.guestPhone = guestPhone;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.cancellationPolicy = cancellationPolicy;
        this.createdAt = createdAt;
        this.holdExpiresAt = holdExpiresAt;
    }

    /** Only lifecycle fields change after a booking is created. */
    void updateLifecycle(BookingStatus status, Instant confirmedAt, Instant cancelledAt) {
        this.status = status;
        this.confirmedAt = confirmedAt;
        this.cancelledAt = cancelledAt;
    }

    @Override
    public UUID getId() {
        return id;
    }

    UUID getPropertyId() {
        return propertyId;
    }

    UUID getRoomTypeId() {
        return roomTypeId;
    }

    LocalDate getCheckIn() {
        return checkIn;
    }

    LocalDate getCheckOut() {
        return checkOut;
    }

    int getGuests() {
        return guests;
    }

    int getRooms() {
        return rooms;
    }

    String getGuestName() {
        return guestName;
    }

    String getGuestEmail() {
        return guestEmail;
    }

    String getGuestPhone() {
        return guestPhone;
    }

    BigDecimal getTotalAmount() {
        return totalAmount;
    }

    String getCurrency() {
        return currency;
    }

    CancellationPolicyType getCancellationPolicy() {
        return cancellationPolicy;
    }

    BookingStatus getStatus() {
        return status;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    Instant getHoldExpiresAt() {
        return holdExpiresAt;
    }

    Instant getConfirmedAt() {
        return confirmedAt;
    }

    Instant getCancelledAt() {
        return cancelledAt;
    }
}
