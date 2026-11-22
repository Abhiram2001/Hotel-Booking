package com.hotelbooking.payment.api.dto;

import com.hotelbooking.payment.domain.CardInstrument;
import com.hotelbooking.payment.domain.PaymentInstrument;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.YearMonth;

public record CardPaymentRequest(
        @NotBlank String cardNumber,
        @NotBlank String holderName,
        @NotNull @Min(1) @Max(12) Integer expiryMonth,
        @NotNull @Min(2000) @Max(2100) Integer expiryYear) implements PaymentRequest {

    @Override
    public PaymentInstrument toInstrument() {
        return new CardInstrument(cardNumber, holderName, YearMonth.of(expiryYear, expiryMonth));
    }

    @Override
    public String toString() {
        return "CardPaymentRequest[masked]";
    }
}
