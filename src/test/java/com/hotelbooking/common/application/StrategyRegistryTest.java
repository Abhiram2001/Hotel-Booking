package com.hotelbooking.common.application;

import com.hotelbooking.common.domain.error.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.hotelbooking.support.DomainAssertions.assertRejected;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StrategyRegistryTest {

    private record Strategy(String key) {
    }

    @Test
    void findsStrategyByKey() {
        Strategy card = new Strategy("CARD");
        StrategyRegistry<String, Strategy> registry =
                new StrategyRegistry<>("processor", List.of(card, new Strategy("UPI")), Strategy::key);

        assertThat(registry.get("CARD")).isSameAs(card);
        assertThat(registry.keys()).containsExactlyInAnyOrder("CARD", "UPI");
    }

    @Test
    void unknownKeyIsAClientError() {
        StrategyRegistry<String, Strategy> registry =
                new StrategyRegistry<>("processor", List.of(new Strategy("CARD")), Strategy::key);

        assertRejected(DomainValidationException.class, "UNSUPPORTED_STRATEGY", () -> registry.get("BITCOIN"));
    }

    @Test
    void twoStrategiesForTheSameKeyFailAtStartup() {
        List<Strategy> duplicates = List.of(new Strategy("CARD"), new Strategy("CARD"));

        assertThatThrownBy(() -> new StrategyRegistry<>("processor", duplicates, Strategy::key))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate processor for CARD");
    }
}
