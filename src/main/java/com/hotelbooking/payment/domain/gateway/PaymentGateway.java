package com.hotelbooking.payment.domain.gateway;

/**
 * Our own contract for an external payment provider. The provider itself is mocked; a real integration is
 * one more implementation of this interface.
 */
public interface PaymentGateway {

    GatewayResult charge(ChargeRequest request);

    GatewayResult refund(RefundRequest request);
}
