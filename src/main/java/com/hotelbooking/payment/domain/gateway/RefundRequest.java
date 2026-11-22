package com.hotelbooking.payment.domain.gateway;

import com.hotelbooking.common.domain.Money;

public record RefundRequest(String merchantReference, String chargeReference, Money amount) {
}
