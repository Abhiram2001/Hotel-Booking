package com.hotelbooking.payment.application.processor;

import com.hotelbooking.payment.domain.PaymentMethod;
import com.hotelbooking.payment.domain.UpiInstrument;
import com.hotelbooking.payment.domain.gateway.PaymentGateway;
import org.springframework.stereotype.Component;

@Component
class UpiPaymentProcessor extends GatewayPaymentProcessor<UpiInstrument> {

    UpiPaymentProcessor(PaymentGateway gateway) {
        super(gateway, UpiInstrument.class);
    }

    @Override
    public PaymentMethod method() {
        return PaymentMethod.UPI;
    }

    @Override
    protected String gatewayToken(UpiInstrument upi) {
        return upi.vpa();
    }
}
