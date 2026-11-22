package com.hotelbooking.cancellation.domain;

import com.hotelbooking.common.domain.Money;
import com.hotelbooking.property.domain.CancellationPolicyType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CancellationPolicyTest {

    private static final Money PAID = Money.inr(10000);

    /** Tiers are deliberately listed out of order: the policy must sort them itself. */
    private final CancellationPolicy moderate = new TieredCancellationPolicy(CancellationPolicyType.MODERATE, List.of(
            new RefundTier(Duration.ofHours(48), 50),
            new RefundTier(Duration.ofDays(7), 100)));

    @ParameterizedTest(name = "{0} hours notice -> {1}%")
    @CsvSource({
            "200, 100",
            "168, 100",
            "167, 50",
            "48, 50",
            "47, 0",
            "1, 0"
    })
    void longestTierTheGuestStillMeetsApplies(long hoursNotice, int expectedPercentage) {
        RefundDecision decision = moderate.evaluate(new CancellationContext(PAID, Duration.ofHours(hoursNotice)));

        assertThat(decision.refundPercentage()).isEqualTo(expectedPercentage);
        assertThat(decision.refundAmount()).isEqualTo(PAID.percentage(expectedPercentage));
        assertThat(decision.isRefundable()).isEqualTo(expectedPercentage > 0);
    }

    @Test
    void nonRefundablePolicyNeverRefunds() {
        RefundDecision decision = new NonRefundablePolicy()
                .evaluate(new CancellationContext(PAID, Duration.ofDays(60)));

        assertThat(decision.isRefundable()).isFalse();
        assertThat(decision.refundAmount()).isEqualTo(Money.zero(Money.INR));
    }
}
