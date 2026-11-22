package com.hotelbooking.payment.application.processor;

import com.hotelbooking.common.application.StrategyRegistry;
import com.hotelbooking.payment.domain.PaymentMethod;
import org.springframework.stereotype.Component;

import java.util.List;

/** All {@link PaymentProcessor} beans, keyed by the method they handle. */
@Component
public class PaymentProcessors {

    private final StrategyRegistry<PaymentMethod, PaymentProcessor> registry;

    public PaymentProcessors(List<PaymentProcessor> processors) {
        this.registry = new StrategyRegistry<>("payment processor", processors, PaymentProcessor::method);
    }

    public PaymentProcessor forMethod(PaymentMethod method) {
        return registry.get(method);
    }
}
