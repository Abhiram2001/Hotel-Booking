package com.hotelbooking.common.domain.error;

/** The request is valid but conflicts with the current state of a resource (no rooms left, wrong state, ...). */
public class ConflictException extends DomainException {

    public ConflictException(String code, String message) {
        super(code, message);
    }
}
