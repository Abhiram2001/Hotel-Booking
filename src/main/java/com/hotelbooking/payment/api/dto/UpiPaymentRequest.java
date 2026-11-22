package com.hotelbooking.payment.api.dto;

import com.hotelbooking.payment.domain.PaymentInstrument;
import com.hotelbooking.payment.domain.UpiInstrument;
import jakarta.validation.constraints.NotBlank;

public record UpiPaymentRequest(@NotBlank String vpa) implements PaymentRequest {

    @Override
    public PaymentInstrument toInstrument() {
        return new UpiInstrument(vpa);
    }
}
