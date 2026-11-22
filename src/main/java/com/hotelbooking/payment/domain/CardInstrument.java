package com.hotelbooking.payment.domain;

import com.hotelbooking.common.domain.Guard;
import com.hotelbooking.common.domain.error.DomainValidationException;

import java.time.YearMonth;

/**
 * Card details. In production the client would send a gateway token instead of the card number, so the
 * service stays out of PCI-DSS scope. {@link #toString()} is masked so the number never reaches logs.
 */
public record CardInstrument(String cardNumber, String holderName, YearMonth expiry) implements PaymentInstrument {

    public CardInstrument {
        cardNumber = Guard.notBlank(cardNumber, "cardNumber").replace(" ", "");
        holderName = Guard.notBlank(holderName, "holderName");
        Guard.notNull(expiry, "expiry");
        if (!cardNumber.matches("\\d{12,19}") || !passesLuhnCheck(cardNumber)) {
            throw new DomainValidationException("INVALID_CARD_NUMBER", "Card number is not valid");
        }
    }

    public boolean isExpiredAt(YearMonth current) {
        return expiry.isBefore(current);
    }

    @Override
    public PaymentMethod method() {
        return PaymentMethod.CARD;
    }

    @Override
    public String maskedIdentifier() {
        return "**** " + cardNumber.substring(cardNumber.length() - 4);
    }

    @Override
    public String fingerprintSource() {
        return cardNumber;
    }

    @Override
    public String toString() {
        return "CardInstrument[" + maskedIdentifier() + "]";
    }

    private static boolean passesLuhnCheck(String digits) {
        int sum = 0;
        boolean doubleIt = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int digit = digits.charAt(i) - '0';
            if (doubleIt) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubleIt = !doubleIt;
        }
        return sum % 10 == 0;
    }
}
