package com.hotelbooking.cancellation.domain;

import com.hotelbooking.common.domain.Guard;

import java.time.Duration;

/** "Cancel at least {@code minimumNotice} before check-in and get {@code refundPercentage}% back." */
public record RefundTier(Duration minimumNotice, int refundPercentage) {

    public RefundTier {
        Guard.notNull(minimumNotice, "minimumNotice");
        Guard.isTrue(!minimumNotice.isNegative(), "minimumNotice cannot be negative");
        Guard.isTrue(refundPercentage >= 0 && refundPercentage <= 100, "refundPercentage must be between 0 and 100");
    }
}
