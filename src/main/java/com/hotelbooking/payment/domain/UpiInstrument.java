package com.hotelbooking.payment.domain;

import com.hotelbooking.common.domain.Guard;
import com.hotelbooking.common.domain.error.DomainValidationException;

import java.util.Locale;
import java.util.regex.Pattern;

/** A UPI virtual payment address such as {@code name@bank}. */
public record UpiInstrument(String vpa) implements PaymentInstrument {

    private static final Pattern VPA = Pattern.compile("^[a-z0-9._-]{2,256}@[a-z][a-z0-9]{1,63}$");

    public UpiInstrument {
        vpa = Guard.notBlank(vpa, "vpa").toLowerCase(Locale.ROOT);
        if (!VPA.matcher(vpa).matches()) {
            throw new DomainValidationException("INVALID_UPI_ADDRESS", "UPI address '%s' is not valid".formatted(vpa));
        }
    }

    @Override
    public PaymentMethod method() {
        return PaymentMethod.UPI;
    }

    @Override
    public String maskedIdentifier() {
        int at = vpa.indexOf('@');
        return vpa.substring(0, Math.min(2, at)) + "***" + vpa.substring(at);
    }

    @Override
    public String fingerprintSource() {
        return vpa;
    }
}
