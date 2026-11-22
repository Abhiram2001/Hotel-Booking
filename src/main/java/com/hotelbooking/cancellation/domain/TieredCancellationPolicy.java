package com.hotelbooking.cancellation.domain;

import com.hotelbooking.common.domain.Guard;
import com.hotelbooking.property.domain.CancellationPolicyType;

import java.util.Comparator;
import java.util.List;

/**
 * Refund depends on how early the guest cancels. The tier with the longest notice the guest still meets
 * applies; with less notice than every tier, nothing is refunded. Most real policies are data for this class.
 */
public final class TieredCancellationPolicy implements CancellationPolicy {

    private final CancellationPolicyType type;
    private final List<RefundTier> tiersByNoticeDescending;

    public TieredCancellationPolicy(CancellationPolicyType type, List<RefundTier> tiers) {
        this.type = Guard.notNull(type, "type");
        Guard.notNull(tiers, "tiers");
        this.tiersByNoticeDescending = tiers.stream()
                .sorted(Comparator.comparing(RefundTier::minimumNotice).reversed())
                .toList();
    }

    @Override
    public CancellationPolicyType type() {
        return type;
    }

    @Override
    public RefundDecision evaluate(CancellationContext context) {
        return tiersByNoticeDescending.stream()
                .filter(tier -> context.noticeBeforeCheckIn().compareTo(tier.minimumNotice()) >= 0)
                .findFirst()
                .map(tier -> RefundDecision.percentageOf(context.amountPaid(), tier.refundPercentage()))
                .orElseGet(() -> RefundDecision.none(context.amountPaid().currency()));
    }
}
