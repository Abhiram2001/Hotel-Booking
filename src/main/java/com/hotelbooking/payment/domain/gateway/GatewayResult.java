package com.hotelbooking.payment.domain.gateway;

public record GatewayResult(boolean successful, String reference, String failureReason) {

    public static GatewayResult success(String reference) {
        return new GatewayResult(true, reference, null);
    }

    public static GatewayResult failure(String reason) {
        return new GatewayResult(false, null, reason);
    }
}
