package com.hotelbooking.common.domain.error;

import java.util.Locale;

public class ResourceNotFoundException extends DomainException {

    public ResourceNotFoundException(String resource, Object id) {
        super(codeFor(resource), "%s %s was not found".formatted(resource, id));
    }

    private static String codeFor(String resource) {
        return resource.toUpperCase(Locale.ROOT).replace(' ', '_') + "_NOT_FOUND";
    }
}
