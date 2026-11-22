package com.hotelbooking.cancellation.domain;

import com.hotelbooking.property.domain.CancellationPolicyType;

/** The guest may cancel (which releases the rooms) but gets nothing back. */
public final class NonRefundablePolicy implements CancellationPolicy {

    @Override
    public CancellationPolicyType type() {
        return CancellationPolicyType.NON_REFUNDABLE;
    }

    @Override
    public RefundDecision evaluate(CancellationContext context) {
        return RefundDecision.none(context.amountPaid().currency());
    }
}
