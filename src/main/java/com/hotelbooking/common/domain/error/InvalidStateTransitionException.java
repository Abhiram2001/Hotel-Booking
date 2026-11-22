package com.hotelbooking.common.domain.error;

public class InvalidStateTransitionException extends ConflictException {

    public InvalidStateTransitionException(String entity, Object id, Enum<?> from, Enum<?> to) {
        super("INVALID_STATE_TRANSITION", "%s %s cannot move from %s to %s".formatted(entity, id, from, to));
    }
}
