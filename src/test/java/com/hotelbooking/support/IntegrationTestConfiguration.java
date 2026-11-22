package com.hotelbooking.support;

import com.hotelbooking.payment.infrastructure.gateway.MockGatewayProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration(proxyBeanMethods = false)
public class IntegrationTestConfiguration {

    @Bean
    @Primary
    MutableClock testClock() {
        return new MutableClock();
    }

    @Bean
    @Primary
    ScriptablePaymentGateway testPaymentGateway(MockGatewayProperties properties) {
        return new ScriptablePaymentGateway(properties.declinedInstruments());
    }
}
