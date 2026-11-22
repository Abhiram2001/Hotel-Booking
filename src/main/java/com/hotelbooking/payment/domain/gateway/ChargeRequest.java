package com.hotelbooking.payment.domain.gateway;

import com.hotelbooking.common.domain.Money;
import com.hotelbooking.payment.domain.PaymentMethod;

/**
 * @param merchantReference our payment id; real gateways use it to de-duplicate repeated charge calls
 * @param instrumentToken   what the gateway charges: card number (a vault token in production), VPA or wallet id
 */
public record ChargeRequest(String merchantReference, Money amount, PaymentMethod method, String instrumentToken) {

    @Override
    public String toString() {
        return "ChargeRequest[" + merchantReference + ", " + amount + ", " + method + "]";
    }
}
