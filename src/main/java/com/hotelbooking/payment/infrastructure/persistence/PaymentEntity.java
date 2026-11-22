package com.hotelbooking.payment.infrastructure.persistence;

import com.hotelbooking.common.infrastructure.persistence.AssignedIdEntity;
import com.hotelbooking.payment.domain.PaymentMethod;
import com.hotelbooking.payment.domain.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment",
        indexes = @Index(name = "idx_payment_booking", columnList = "booking_id, status"),
        uniqueConstraints = @UniqueConstraint(name = "uk_payment_idempotency_key", columnNames = "idempotency_key"))
class PaymentEntity extends AssignedIdEntity<UUID> {

    @Id
    private UUID id;

    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Column(name = "masked_instrument", nullable = false)
    private String maskedInstrument;

    @Column(name = "idempotency_key", nullable = false, length = 64)
    private String idempotencyKey;

    @Column(name = "request_fingerprint", nullable = false, length = 64)
    private String requestFingerprint;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(name = "gateway_reference")
    private String gatewayReference;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "refund_amount", precision = 12, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "refund_reference")
    private String refundReference;

    protected PaymentEntity() {
    }

    PaymentEntity(UUID id, UUID bookingId, BigDecimal amount, String currency, PaymentMethod method,
                  String maskedInstrument, String idempotencyKey, String requestFingerprint, Instant createdAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.amount = amount;
        this.currency = currency;
        this.method = method;
        this.maskedInstrument = maskedInstrument;
        this.idempotencyKey = idempotencyKey;
        this.requestFingerprint = requestFingerprint;
        this.createdAt = createdAt;
    }

    void updateOutcome(PaymentStatus status, String gatewayReference, String failureReason, BigDecimal refundAmount,
                       String refundReference) {
        this.status = status;
        this.gatewayReference = gatewayReference;
        this.failureReason = failureReason;
        this.refundAmount = refundAmount;
        this.refundReference = refundReference;
    }

    @Override
    public UUID getId() {
        return id;
    }

    UUID getBookingId() {
        return bookingId;
    }

    BigDecimal getAmount() {
        return amount;
    }

    String getCurrency() {
        return currency;
    }

    PaymentMethod getMethod() {
        return method;
    }

    String getMaskedInstrument() {
        return maskedInstrument;
    }

    String getIdempotencyKey() {
        return idempotencyKey;
    }

    String getRequestFingerprint() {
        return requestFingerprint;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    PaymentStatus getStatus() {
        return status;
    }

    String getGatewayReference() {
        return gatewayReference;
    }

    String getFailureReason() {
        return failureReason;
    }

    BigDecimal getRefundAmount() {
        return refundAmount;
    }

    String getRefundReference() {
        return refundReference;
    }
}
