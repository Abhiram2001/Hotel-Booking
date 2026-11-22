package com.hotelbooking.common.domain;

import com.hotelbooking.common.domain.error.DomainValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

/** A non-negative amount in one currency, always held at two decimal places. */
public record Money(BigDecimal amount, Currency currency) implements Comparable<Money> {

    public static final Currency INR = Currency.getInstance("INR");

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    public Money {
        Guard.notNull(amount, "amount");
        Guard.notNull(currency, "currency");
        Guard.isTrue(amount.signum() >= 0, "Money amount cannot be negative");
        amount = amount.setScale(SCALE, ROUNDING);
    }

    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    public static Money inr(BigDecimal amount) {
        return new Money(amount, INR);
    }

    public static Money inr(long amount) {
        return inr(BigDecimal.valueOf(amount));
    }

    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public Money plus(Money other) {
        requireSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    public Money times(int factor) {
        Guard.isTrue(factor >= 0, "Multiplier cannot be negative");
        return new Money(amount.multiply(BigDecimal.valueOf(factor)), currency);
    }

    public Money dividedBy(int divisor) {
        Guard.positive(divisor, "divisor");
        return new Money(amount.divide(BigDecimal.valueOf(divisor), SCALE, ROUNDING), currency);
    }

    public Money percentage(int percent) {
        Guard.isTrue(percent >= 0 && percent <= 100, "Percentage must be between 0 and 100");
        return new Money(amount.multiply(BigDecimal.valueOf(percent)).divide(HUNDRED, SCALE, ROUNDING), currency);
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    public boolean isGreaterThan(Money other) {
        return compareTo(other) > 0;
    }

    public boolean isLessThan(Money other) {
        return compareTo(other) < 0;
    }

    @Override
    public int compareTo(Money other) {
        requireSameCurrency(other);
        return amount.compareTo(other.amount);
    }

    private void requireSameCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new DomainValidationException("CURRENCY_MISMATCH",
                    "Cannot combine %s with %s".formatted(currency, other.currency));
        }
    }

    @Override
    public String toString() {
        return currency.getCurrencyCode() + " " + amount.toPlainString();
    }
}
