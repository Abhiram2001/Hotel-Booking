package com.hotelbooking.common.domain.error;

/** The request is well formed but a business rule forbids it (cancellation window closed, key reused, ...). */
public class BusinessRuleViolationException extends DomainException {

    public BusinessRuleViolationException(String code, String message) {
        super(code, message);
    }
}
