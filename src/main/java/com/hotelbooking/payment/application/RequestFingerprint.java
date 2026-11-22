package com.hotelbooking.payment.application;

import com.hotelbooking.booking.domain.BookingId;
import com.hotelbooking.payment.domain.PaymentInstrument;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** SHA-256 of what the request asks for, so a reused Idempotency-Key with a different request is detected. */
final class RequestFingerprint {

    private RequestFingerprint() {
    }

    static String of(BookingId bookingId, PaymentInstrument instrument) {
        String source = bookingId + "|" + instrument.method() + "|" + instrument.fingerprintSource();
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(source.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is required by every Java runtime", ex);
        }
    }
}
