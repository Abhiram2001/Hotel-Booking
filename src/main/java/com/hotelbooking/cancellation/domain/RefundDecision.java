package com.hotelbooking.cancellation.domain;

import com.hotelbooking.common.domain.Money;

import java.util.Currency;

public record RefundDecision(Money refundAmount, int refundPercentage) {

    public static RefundDecision percentageOf(Money amountPaid, int percentage) {
        return new RefundDecision(amountPaid.percentage(percentage), percentage);
    }

    public static RefundDecision none(Currency currency) {
        return new RefundDecision(Money.zero(currency), 0);
    }

    public boolean isRefundable() {
        return !refundAmount.isZero();
    }
}
