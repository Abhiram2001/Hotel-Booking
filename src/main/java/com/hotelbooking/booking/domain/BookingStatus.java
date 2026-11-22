package com.hotelbooking.booking.domain;

import com.hotelbooking.common.domain.StateTransitions;

/**
 * <pre>
 * PENDING_PAYMENT --payment succeeds--> CONFIRMED --cancel--> CANCELLED
 * PENDING_PAYMENT --cancel--> CANCELLED
 * PENDING_PAYMENT --hold times out--> EXPIRED
 * </pre>
 * A failed payment leaves the booking in PENDING_PAYMENT so the guest can retry until the hold expires.
 */
public enum BookingStatus {
    PENDING_PAYMENT,
    CONFIRMED,
    CANCELLED,
    EXPIRED;

    private static final StateTransitions<BookingStatus> TRANSITIONS = StateTransitions.builder(BookingStatus.class)
            .allow(PENDING_PAYMENT, CONFIRMED, CANCELLED, EXPIRED)
            .allow(CONFIRMED, CANCELLED)
            .build();

    public boolean canTransitionTo(BookingStatus target) {
        return TRANSITIONS.isAllowed(this, target);
    }
}
