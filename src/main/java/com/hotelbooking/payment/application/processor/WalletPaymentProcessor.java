package com.hotelbooking.payment.application.processor;

import com.hotelbooking.payment.domain.PaymentMethod;
import com.hotelbooking.payment.domain.WalletInstrument;
import com.hotelbooking.payment.domain.gateway.PaymentGateway;
import org.springframework.stereotype.Component;

@Component
class WalletPaymentProcessor extends GatewayPaymentProcessor<WalletInstrument> {

    WalletPaymentProcessor(PaymentGateway gateway) {
        super(gateway, WalletInstrument.class);
    }

    @Override
    public PaymentMethod method() {
        return PaymentMethod.WALLET;
    }

    @Override
    protected String gatewayToken(WalletInstrument wallet) {
        return wallet.provider() + ":" + wallet.walletId();
    }
}
