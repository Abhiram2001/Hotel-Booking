package com.hotelbooking.payment.domain;

import com.hotelbooking.common.domain.error.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;

import static com.hotelbooking.support.DomainAssertions.assertRejected;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentInstrumentTest {

    private static final YearMonth EXPIRY = YearMonth.of(2031, 12);

    @Test
    void cardNumberMustPassTheLuhnCheck() {
        assertRejected(DomainValidationException.class, "INVALID_CARD_NUMBER",
                () -> new CardInstrument("4242424242424241", "Asha Rao", EXPIRY));
        assertRejected(DomainValidationException.class, "INVALID_CARD_NUMBER",
                () -> new CardInstrument("4242-4242", "Asha Rao", EXPIRY));
    }

    @Test
    void cardNumberNeverAppearsInMaskedFormOrLogs() {
        CardInstrument card = new CardInstrument("4242 4242 4242 4242", "Asha Rao", EXPIRY);

        assertThat(card.maskedIdentifier()).isEqualTo("**** 4242");
        assertThat(card.toString()).doesNotContain("424242424242");
    }

    @Test
    void cardExpiresAfterItsExpiryMonth() {
        CardInstrument card = new CardInstrument("4242424242424242", "Asha Rao", EXPIRY);

        assertThat(card.isExpiredAt(EXPIRY)).isFalse();
        assertThat(card.isExpiredAt(EXPIRY.plusMonths(1))).isTrue();
    }

    @Test
    void upiAddressIsValidatedAndNormalised() {
        UpiInstrument upi = new UpiInstrument("Asha.Rao@OKICICI");

        assertThat(upi.vpa()).isEqualTo("asha.rao@okicici");
        assertThat(upi.maskedIdentifier()).isEqualTo("as***@okicici");
        assertRejected(DomainValidationException.class, "INVALID_UPI_ADDRESS", () -> new UpiInstrument("no-at-sign"));
    }

    @Test
    void walletShowsProviderAndLastDigitsOnly() {
        WalletInstrument wallet = new WalletInstrument(WalletProvider.PAYTM, "9812345678");

        assertThat(wallet.maskedIdentifier()).isEqualTo("PAYTM ****5678");
        assertThat(wallet.method()).isEqualTo(PaymentMethod.WALLET);
    }
}
