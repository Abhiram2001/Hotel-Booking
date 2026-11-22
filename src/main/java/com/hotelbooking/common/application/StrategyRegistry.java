package com.hotelbooking.common.application;

import com.hotelbooking.common.domain.error.DomainValidationException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Looks up a strategy by its key (payment method, cancellation policy type, ...). Spring injects every
 * implementation of a strategy interface, so a new strategy is one new class with no edits to callers.
 * Two strategies claiming the same key fail fast at startup.
 */
public final class StrategyRegistry<K, S> {

    private final String strategyName;
    private final Map<K, S> strategies;

    public StrategyRegistry(String strategyName, Collection<? extends S> candidates, Function<? super S, ? extends K> keyOf) {
        this.strategyName = strategyName;
        Map<K, S> byKey = new HashMap<>();
        for (S candidate : candidates) {
            K key = keyOf.apply(candidate);
            S previous = byKey.putIfAbsent(key, candidate);
            if (previous != null) {
                throw new IllegalStateException("Duplicate %s for %s: %s and %s".formatted(
                        strategyName, key, previous.getClass().getSimpleName(), candidate.getClass().getSimpleName()));
            }
        }
        this.strategies = Map.copyOf(byKey);
    }

    public S get(K key) {
        S strategy = strategies.get(key);
        if (strategy == null) {
            throw new DomainValidationException("UNSUPPORTED_STRATEGY", "No %s registered for %s".formatted(strategyName, key));
        }
        return strategy;
    }

    public Set<K> keys() {
        return strategies.keySet();
    }
}
