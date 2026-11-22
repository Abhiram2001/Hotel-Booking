package com.hotelbooking.property.domain;

/**
 * The cancellation terms a property offers. The refund rules for each type live in the cancellation module;
 * a booking copies the type at booking time so later policy changes do not affect existing bookings.
 */
public enum CancellationPolicyType {
    FLEXIBLE,
    MODERATE,
    NON_REFUNDABLE
}
