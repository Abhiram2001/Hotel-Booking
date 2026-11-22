package com.hotelbooking.pricing.config;

import com.hotelbooking.pricing.domain.BasePricingStrategy;
import com.hotelbooking.pricing.domain.PricingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Extension point for dynamic pricing. To add rules, register a different {@link PricingStrategy} here, for
 * example a decorator that applies weekend or occupancy adjustments on top of {@link BasePricingStrategy}.
 */
@Configuration
class PricingConfiguration {

    @Bean
    PricingStrategy pricingStrategy() {
        return new BasePricingStrategy();
    }
}
