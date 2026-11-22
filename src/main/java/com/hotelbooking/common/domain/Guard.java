package com.hotelbooking.common.domain;

import com.hotelbooking.common.domain.error.DomainValidationException;

/** Small precondition helpers so domain constructors stay short and throw a consistent exception type. */
public final class Guard {

    private Guard() {
    }

    public static <T> T notNull(T value, String field) {
        if (value == null) {
            throw new DomainValidationException(field + " is required");
        }
        return value;
    }

    public static String notBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException(field + " must not be blank");
        }
        return value.trim();
    }

    public static int positive(int value, String field) {
        if (value <= 0) {
            throw new DomainValidationException(field + " must be greater than zero");
        }
        return value;
    }

    public static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new DomainValidationException(message);
        }
    }
}
