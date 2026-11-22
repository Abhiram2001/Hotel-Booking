package com.hotelbooking.common.domain.error;

/**
 * Base type for every business error. The {@code code} is a stable, machine-readable identifier that the API
 * layer returns to clients; the HTTP status is chosen by the API layer from the exception category.
 */
public abstract class DomainException extends RuntimeException {

    private final String code;

    protected DomainException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
