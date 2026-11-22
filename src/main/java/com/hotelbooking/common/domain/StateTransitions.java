package com.hotelbooking.common.domain;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Declarative table of allowed moves between the states of an enum. Status enums declare their table once,
 * and aggregates ask it before changing state, so the lifecycle rules live in a single readable place.
 */
public final class StateTransitions<S extends Enum<S>> {

    private final Map<S, Set<S>> allowed;

    private StateTransitions(Map<S, Set<S>> allowed) {
        this.allowed = allowed;
    }

    public static <S extends Enum<S>> Builder<S> builder(Class<S> type) {
        return new Builder<>(type);
    }

    public boolean isAllowed(S from, S to) {
        return allowed.getOrDefault(from, Set.of()).contains(to);
    }

    public static final class Builder<S extends Enum<S>> {

        private final Class<S> type;
        private final Map<S, Set<S>> allowed;

        private Builder(Class<S> type) {
            this.type = type;
            this.allowed = new EnumMap<>(type);
        }

        @SafeVarargs
        public final Builder<S> allow(S from, S first, S... rest) {
            allowed.computeIfAbsent(from, key -> EnumSet.noneOf(type)).addAll(EnumSet.of(first, rest));
            return this;
        }

        public StateTransitions<S> build() {
            Map<S, Set<S>> copy = new EnumMap<>(type);
            allowed.forEach((from, targets) -> copy.put(from, Collections.unmodifiableSet(EnumSet.copyOf(targets))));
            return new StateTransitions<>(Collections.unmodifiableMap(copy));
        }
    }
}
