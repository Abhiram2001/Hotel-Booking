package com.hotelbooking.payment.domain;

import com.hotelbooking.common.domain.StateTransitions;

/**
 * <pre>
 * INITIATED --gateway approves--> SUCCEEDED --refund requested--> REFUND_PENDING --gateway approves--> REFUNDED
 * INITIATED --gateway declines--> FAILED                                         \--gateway declines--> REFUND_FAILED
 * </pre>
 * REFUND_FAILED is final in this service: it keeps the refund amount for an operator or a reconciliation job.
 */
public enum PaymentStatus {
    INITIATED,
    SUCCEEDED,
    FAILED,
    REFUND_PENDING,
    REFUNDED,
    REFUND_FAILED;

    private static final StateTransitions<PaymentStatus> TRANSITIONS = StateTransitions.builder(PaymentStatus.class)
            .allow(INITIATED, SUCCEEDED, FAILED)
            .allow(SUCCEEDED, REFUND_PENDING)
            .allow(REFUND_PENDING, REFUNDED, REFUND_FAILED)
            .build();

    public boolean canTransitionTo(PaymentStatus target) {
        return TRANSITIONS.isAllowed(this, target);
    }
}
