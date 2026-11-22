package com.hotelbooking.payment.domain;

import com.hotelbooking.common.domain.Guard;
import com.hotelbooking.common.domain.error.DomainValidationException;

import java.util.regex.Pattern;

/** Client-chosen key (typically a UUID) that makes retries of the same payment request safe. */
public record IdempotencyKey(String value) {

    private static final Pattern FORMAT = Pattern.compile("^[A-Za-z0-9_-]{8,64}$");

    public IdempotencyKey {
        value = Guard.notBlank(value, "Idempotency-Key");
        if (!FORMAT.matcher(value).matches()) {
            throw new DomainValidationException("INVALID_IDEMPOTENCY_KEY",
                    "Idempotency-Key must be 8 to 64 characters: letters, digits, '-' or '_'");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
