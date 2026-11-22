package com.hotelbooking.booking.domain;

import com.hotelbooking.common.domain.Guard;
import com.hotelbooking.common.domain.Money;
import com.hotelbooking.common.domain.error.BusinessRuleViolationException;
import com.hotelbooking.common.domain.error.ConflictException;
import com.hotelbooking.common.domain.error.InvalidStateTransitionException;
import com.hotelbooking.property.domain.CancellationPolicyType;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * A guest's booking. Price and cancellation policy are copied in when the booking is placed, so later changes
 * to the property do not change what the guest agreed to. All state changes go through {@link BookingStatus}.
 */
public final class Booking {

    private final BookingId id;
    private final Stay stay;
    private final GuestDetails guest;
    private final Money totalAmount;
    private final CancellationPolicyType cancellationPolicy;
    private final Instant createdAt;
    private final Instant holdExpiresAt;
    private BookingStatus status;
    private Instant confirmedAt;
    private Instant cancelledAt;

    private Booking(BookingId id, Stay stay, GuestDetails guest, Money totalAmount,
                    CancellationPolicyType cancellationPolicy, BookingStatus status, Instant createdAt,
                    Instant holdExpiresAt, Instant confirmedAt, Instant cancelledAt) {
        this.id = Guard.notNull(id, "bookingId");
        this.stay = Guard.notNull(stay, "stay");
        this.guest = Guard.notNull(guest, "guest");
        this.totalAmount = Guard.notNull(totalAmount, "totalAmount");
        this.cancellationPolicy = Guard.notNull(cancellationPolicy, "cancellationPolicy");
        this.status = Guard.notNull(status, "status");
        this.createdAt = Guard.notNull(createdAt, "createdAt");
        this.holdExpiresAt = Guard.notNull(holdExpiresAt, "holdExpiresAt");
        this.confirmedAt = confirmedAt;
        this.cancelledAt = cancelledAt;
    }

    /** A new booking holds its rooms for {@code holdDuration} while it waits for payment. */
    public static Booking place(Stay stay, GuestDetails guest, Money totalAmount,
                                CancellationPolicyType cancellationPolicy, Instant now, Duration holdDuration) {
        Guard.isTrue(!totalAmount.isZero(), "A booking must have a positive amount");
        return new Booking(BookingId.newId(), stay, guest, totalAmount, cancellationPolicy,
                BookingStatus.PENDING_PAYMENT, now, now.plus(holdDuration), null, null);
    }

    public static Booking restore(BookingId id, Stay stay, GuestDetails guest, Money totalAmount,
                                  CancellationPolicyType cancellationPolicy, BookingStatus status, Instant createdAt,
                                  Instant holdExpiresAt, Instant confirmedAt, Instant cancelledAt) {
        return new Booking(id, stay, guest, totalAmount, cancellationPolicy, status, createdAt, holdExpiresAt,
                confirmedAt, cancelledAt);
    }

    public boolean isAwaitingPayment() {
        return status == BookingStatus.PENDING_PAYMENT;
    }

    public boolean isHoldExpired(Instant now) {
        return isAwaitingPayment() && !now.isBefore(holdExpiresAt);
    }

    /** Guards the start of a payment attempt: the booking must be unpaid and its hold still valid. */
    public void ensureAwaitingPayment(Instant now) {
        if (!isAwaitingPayment()) {
            throw new ConflictException("BOOKING_NOT_PAYABLE", "Booking %s is %s and cannot be paid".formatted(id, status));
        }
        if (isHoldExpired(now)) {
            throw new ConflictException("BOOKING_HOLD_EXPIRED",
                    "The hold on booking %s expired at %s".formatted(id, holdExpiresAt));
        }
    }

    /**
     * Confirms after a successful charge. A charge that started before the hold expired may finish after it;
     * that is still accepted as long as the booking has not been expired yet, because its rooms are still held.
     */
    public void confirm(Instant now) {
        moveTo(BookingStatus.CONFIRMED);
        confirmedAt = now;
    }

    public BookingCancellation cancel(Instant now, Instant checkInAt) {
        requireTransition(BookingStatus.CANCELLED);
        if (!now.isBefore(checkInAt)) {
            throw new BusinessRuleViolationException("CANCELLATION_WINDOW_CLOSED",
                    "Booking %s can no longer be cancelled: check-in was at %s".formatted(id, checkInAt));
        }
        BookingStatus previous = status;
        moveTo(BookingStatus.CANCELLED);
        cancelledAt = now;
        return new BookingCancellation(this, previous, Duration.between(now, checkInAt));
    }

    public void expireHold(Instant now) {
        requireTransition(BookingStatus.EXPIRED);
        if (!isHoldExpired(now)) {
            throw new BusinessRuleViolationException("HOLD_STILL_ACTIVE",
                    "The hold on booking %s is valid until %s".formatted(id, holdExpiresAt));
        }
        moveTo(BookingStatus.EXPIRED);
    }

    private void moveTo(BookingStatus target) {
        requireTransition(target);
        status = target;
    }

    private void requireTransition(BookingStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new InvalidStateTransitionException("Booking", id, status, target);
        }
    }

    public BookingId id() {
        return id;
    }

    public Stay stay() {
        return stay;
    }

    public GuestDetails guest() {
        return guest;
    }

    public Money totalAmount() {
        return totalAmount;
    }

    public CancellationPolicyType cancellationPolicy() {
        return cancellationPolicy;
    }

    public BookingStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant holdExpiresAt() {
        return holdExpiresAt;
    }

    public Instant confirmedAt() {
        return confirmedAt;
    }

    public Instant cancelledAt() {
        return cancelledAt;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Booking that && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
