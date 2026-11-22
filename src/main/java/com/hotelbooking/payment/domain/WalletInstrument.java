package com.hotelbooking.payment.domain;

import com.hotelbooking.common.domain.Guard;

public record WalletInstrument(WalletProvider provider, String walletId) implements PaymentInstrument {

    public WalletInstrument {
        Guard.notNull(provider, "provider");
        walletId = Guard.notBlank(walletId, "walletId");
    }

    @Override
    public PaymentMethod method() {
        return PaymentMethod.WALLET;
    }

    @Override
    public String maskedIdentifier() {
        String tail = walletId.length() <= 4 ? walletId : walletId.substring(walletId.length() - 4);
        return provider + " ****" + tail;
    }

    @Override
    public String fingerprintSource() {
        return provider + ":" + walletId;
    }
}
