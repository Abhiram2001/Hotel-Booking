package com.hotelbooking.payment.api.dto;

import com.hotelbooking.payment.domain.PaymentInstrument;
import com.hotelbooking.payment.domain.WalletInstrument;
import com.hotelbooking.payment.domain.WalletProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WalletPaymentRequest(@NotNull WalletProvider provider, @NotBlank String walletId) implements PaymentRequest {

    @Override
    public PaymentInstrument toInstrument() {
        return new WalletInstrument(provider, walletId);
    }
}
