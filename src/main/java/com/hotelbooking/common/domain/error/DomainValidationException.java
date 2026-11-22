package com.hotelbooking.common.domain.error;

/** The input breaks an invariant of a domain object (bad value, bad combination of values). */
public class DomainValidationException extends DomainException {

    public DomainValidationException(String message) {
        this("VALIDATION_FAILED", message);
    }

    public DomainValidationException(String code, String message) {
        super(code, message);
    }
}
