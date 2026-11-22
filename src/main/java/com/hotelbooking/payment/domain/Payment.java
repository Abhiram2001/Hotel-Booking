package com.hotelbooking.payment.domain;

import com.hotelbooking.booking.domain.BookingId;
import com.hotelbooking.common.domain.Guard;
import com.hotelbooking.common.domain.Money;
import com.hotelbooking.common.domain.error.BusinessRuleViolationException;
import com.hotelbooking.common.domain.error.InvalidStateTransitionException;

import java.time.Instant;
import java.util.Objects;

/**
 * One attempt to pay for a booking, and its refund if there is one. The amount always comes from the
 * booking, never from the client. The idempotency key and request fingerprint make retries safe.
 */
public final class Payment {

    private final PaymentId id;
    private final BookingId bookingId;
    private final Money amount;
    private final PaymentMethod method;
    private final String maskedInstrument;
    private final IdempotencyKey idempotencyKey;
    private final String requestFingerprint;
    private final Instant createdAt;
    private PaymentStatus status;
    private String gatewayReference;
    private String failureReason;
    private Money refundAmount;
    private String refundReference;

    private Payment(PaymentId id, BookingId bookingId, Money amount, PaymentMethod method, String maskedInstrument,
                    IdempotencyKey idempotencyKey, String requestFingerprint, Instant createdAt, PaymentStatus status) {
        this.id = Guard.notNull(id, "paymentId");
        this.bookingId = Guard.notNull(bookingId, "bookingId");
        this.amount = Guard.notNull(amount, "amount");
        this.method = Guard.notNull(method, "method");
        this.maskedInstrument = Guard.notBlank(maskedInstrument, "maskedInstrument");
        this.idempotencyKey = Guard.notNull(idempotencyKey, "idempotencyKey");
        this.requestFingerprint = Guard.notBlank(requestFingerprint, "requestFingerprint");
        this.createdAt = Guard.notNull(createdAt, "createdAt");
        this.status = Guard.notNull(status, "status");
    }

    public static Payment initiate(BookingId bookingId, Money amount, PaymentInstrument instrument,
                                   IdempotencyKey idempotencyKey, String requestFingerprint, Instant now) {
        return new Payment(PaymentId.newId(), bookingId, amount, instrument.method(), instrument.maskedIdentifier(),
                idempotencyKey, requestFingerprint, now, PaymentStatus.INITIATED);
    }

    public static Payment restore(PaymentId id, BookingId bookingId, Money amount, PaymentMethod method,
                                  String maskedInstrument, IdempotencyKey idempotencyKey, String requestFingerprint,
                                  Instant createdAt, PaymentStatus status, String gatewayReference,
                                  String failureReason, Money refundAmount, String refundReference) {
        Payment payment = new Payment(id, bookingId, amount, method, maskedInstrument, idempotencyKey,
                requestFingerprint, createdAt, status);
        payment.gatewayReference = gatewayReference;
        payment.failureReason = failureReason;
        payment.refundAmount = refundAmount;
        payment.refundReference = refundReference;
        return payment;
    }

    /** The same key may only be replayed with the same request; anything else is a client bug. */
    public void ensureSameRequest(String fingerprint) {
        if (!requestFingerprint.equals(fingerprint)) {
            throw new BusinessRuleViolationException("IDEMPOTENCY_KEY_REUSED",
                    "Idempotency-Key %s was already used for a different payment request".formatted(idempotencyKey));
        }
    }

    public boolean isInFlight() {
        return status == PaymentStatus.INITIATED;
    }

    public void markSucceeded(String chargeReference) {
        moveTo(PaymentStatus.SUCCEEDED);
        gatewayReference = Guard.notBlank(chargeReference, "chargeReference");
    }

    public void markFailed(String reason) {
        moveTo(PaymentStatus.FAILED);
        failureReason = reason;
    }

    public void requestRefund(Money amountToRefund) {
        Guard.isTrue(!amountToRefund.isZero(), "Refund amount must be greater than zero");
        Guard.isTrue(!amountToRefund.isGreaterThan(amount), "Refund cannot exceed the amount paid");
        moveTo(PaymentStatus.REFUND_PENDING);
        refundAmount = amountToRefund;
        failureReason = null;
    }

    public void markRefunded(String reference) {
        moveTo(PaymentStatus.REFUNDED);
        refundReference = Guard.notBlank(reference, "refundReference");
    }

    public void markRefundFailed(String reason) {
        moveTo(PaymentStatus.REFUND_FAILED);
        failureReason = reason;
    }

    private void moveTo(PaymentStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new InvalidStateTransitionException("Payment", id, status, target);
        }
        status = target;
    }

    public PaymentId id() {
        return id;
    }

    public BookingId bookingId() {
        return bookingId;
    }

    public Money amount() {
        return amount;
    }

    public PaymentMethod method() {
        return method;
    }

    public String maskedInstrument() {
        return maskedInstrument;
    }

    public IdempotencyKey idempotencyKey() {
        return idempotencyKey;
    }

    public String requestFingerprint() {
        return requestFingerprint;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public PaymentStatus status() {
        return status;
    }

    public String gatewayReference() {
        return gatewayReference;
    }

    public String failureReason() {
        return failureReason;
    }

    public Money refundAmount() {
        return refundAmount;
    }

    public String refundReference() {
        return refundReference;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Payment that && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
