package com.hotelbooking.cancellation.domain;

import com.hotelbooking.common.domain.Guard;
import com.hotelbooking.common.domain.Money;

import java.time.Duration;

/** The facts a policy may use. Add fields here when a new policy needs more (e.g. time since booking). */
public record CancellationContext(Money amountPaid, Duration noticeBeforeCheckIn) {

    public CancellationContext {
        Guard.notNull(amountPaid, "amountPaid");
        Guard.notNull(noticeBeforeCheckIn, "noticeBeforeCheckIn");
    }
}
