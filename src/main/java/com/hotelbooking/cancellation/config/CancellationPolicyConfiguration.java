package com.hotelbooking.cancellation.config;

import com.hotelbooking.cancellation.domain.CancellationPolicy;
import com.hotelbooking.cancellation.domain.NonRefundablePolicy;
import com.hotelbooking.cancellation.domain.RefundTier;
import com.hotelbooking.cancellation.domain.TieredCancellationPolicy;
import com.hotelbooking.property.domain.CancellationPolicyType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

/** The refund rules behind each policy type. Policies are plain domain classes; this wires them in. */
@Configuration
class CancellationPolicyConfiguration {

    /** Full refund up to 24 hours before check-in. */
    @Bean
    CancellationPolicy flexibleCancellationPolicy() {
        return new TieredCancellationPolicy(CancellationPolicyType.FLEXIBLE,
                List.of(new RefundTier(Duration.ofHours(24), 100)));
    }

    /** Full refund up to 7 days before check-in, half up to 48 hours before. */
    @Bean
    CancellationPolicy moderateCancellationPolicy() {
        return new TieredCancellationPolicy(CancellationPolicyType.MODERATE, List.of(
                new RefundTier(Duration.ofDays(7), 100),
                new RefundTier(Duration.ofHours(48), 50)));
    }

    @Bean
    CancellationPolicy nonRefundableCancellationPolicy() {
        return new NonRefundablePolicy();
    }
}
