package com.hotelbooking.payment.domain;

/**
 * Method-specific payment details (card, UPI address, wallet). Raw details are only used to call the gateway
 * and are never stored: payments keep the masked form.
 */
public interface PaymentInstrument {

    PaymentMethod method();

    /** Safe to store and display, e.g. "**** 4242". */
    String maskedIdentifier();

    /** Identifies the instrument for idempotency checks. Hashed before it is stored. */
    String fingerprintSource();
}
