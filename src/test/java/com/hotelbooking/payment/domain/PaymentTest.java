package com.hotelbooking.payment.domain;

import com.hotelbooking.booking.domain.BookingId;
import com.hotelbooking.common.domain.Money;
import com.hotelbooking.common.domain.error.BusinessRuleViolationException;
import com.hotelbooking.common.domain.error.DomainValidationException;
import com.hotelbooking.common.domain.error.InvalidStateTransitionException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static com.hotelbooking.support.DomainAssertions.assertRejected;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentTest {

    private final Payment payment = Payment.initiate(BookingId.newId(), Money.inr(8000), new UpiInstrument("asha@okicici"),
            new IdempotencyKey("key-00000001"), "fingerprint-a", Instant.parse("2030-06-01T04:30:00Z"));

    @Test
    void storesOnlyTheMaskedInstrument() {
        assertThat(payment.status()).isEqualTo(PaymentStatus.INITIATED);
        assertThat(payment.isInFlight()).isTrue();
        assertThat(payment.maskedInstrument()).isEqualTo("as***@okicici");
    }

    @Test
    void successfulPaymentCanBePartlyRefunded() {
        payment.markSucceeded("CHG-1");
        payment.requestRefund(Money.inr(4000));
        payment.markRefunded("RFD-1");

        assertThat(payment.status()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(payment.refundAmount()).isEqualTo(Money.inr(4000));
        assertThat(payment.refundReference()).isEqualTo("RFD-1");
    }

    @Test
    void failedPaymentCannotSucceedLaterOrBeRefunded() {
        payment.markFailed("Declined");

        assertThatThrownBy(() -> payment.markSucceeded("CHG-1")).isInstanceOf(InvalidStateTransitionException.class);
        assertThatThrownBy(() -> payment.requestRefund(Money.inr(1))).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void refundCannotExceedTheAmountPaid() {
        payment.markSucceeded("CHG-1");

        assertThatThrownBy(() -> payment.requestRefund(Money.inr(8001))).isInstanceOf(DomainValidationException.class);
        assertThat(payment.status()).isEqualTo(PaymentStatus.SUCCEEDED);
    }

    @Test
    void failedRefundIsFinal() {
        payment.markSucceeded("CHG-1");
        payment.requestRefund(Money.inr(8000));
        payment.markRefundFailed("Gateway rejected refund");

        assertThat(payment.status()).isEqualTo(PaymentStatus.REFUND_FAILED);
        assertThat(payment.refundAmount()).isEqualTo(Money.inr(8000));
        assertThatThrownBy(() -> payment.markRefunded("RFD-1")).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void idempotencyKeyCanOnlyBeReplayedWithTheSameRequest() {
        payment.ensureSameRequest("fingerprint-a");

        assertRejected(BusinessRuleViolationException.class, "IDEMPOTENCY_KEY_REUSED",
                () -> payment.ensureSameRequest("fingerprint-b"));
    }

    @Test
    void idempotencyKeyMustHaveASafeFormat() {
        assertRejected(DomainValidationException.class, "INVALID_IDEMPOTENCY_KEY", () -> new IdempotencyKey("short"));
        assertRejected(DomainValidationException.class, "INVALID_IDEMPOTENCY_KEY", () -> new IdempotencyKey("has spaces in it"));
    }
}
