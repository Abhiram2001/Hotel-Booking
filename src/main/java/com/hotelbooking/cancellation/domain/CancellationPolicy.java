package com.hotelbooking.cancellation.domain;

import com.hotelbooking.property.domain.CancellationPolicyType;

/**
 * Decides how much of a paid amount to refund when a confirmed booking is cancelled. Each implementation
 * handles one {@link CancellationPolicyType}; a new policy is a new implementation registered as a bean.
 */
public interface CancellationPolicy {

    CancellationPolicyType type();

    RefundDecision evaluate(CancellationContext context);
}
