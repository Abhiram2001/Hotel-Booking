package com.hotelbooking.payment.infrastructure.persistence;

import com.hotelbooking.booking.domain.BookingId;
import com.hotelbooking.common.domain.Money;
import com.hotelbooking.payment.domain.IdempotencyKey;
import com.hotelbooking.payment.domain.Payment;
import com.hotelbooking.payment.domain.PaymentId;
import com.hotelbooking.payment.domain.PaymentRepository;
import com.hotelbooking.payment.domain.PaymentStatus;
import org.springframework.stereotype.Repository;

import java.util.Currency;
import java.util.List;
import java.util.Optional;

@Repository
class PaymentRepositoryAdapter implements PaymentRepository {

    private final PaymentJpaRepository jpa;

    PaymentRepositoryAdapter(PaymentJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Payment save(Payment payment) {
        PaymentEntity entity = jpa.findById(payment.id().value()).orElseGet(() -> toNewEntity(payment));
        entity.updateOutcome(payment.status(), payment.gatewayReference(), payment.failureReason(),
                payment.refundAmount() == null ? null : payment.refundAmount().amount(), payment.refundReference());
        jpa.save(entity);
        return payment;
    }

    @Override
    public Optional<Payment> findById(PaymentId id) {
        return jpa.findById(id.value()).map(PaymentRepositoryAdapter::toDomain);
    }

    @Override
    public Optional<Payment> findByIdempotencyKey(IdempotencyKey key) {
        return jpa.findByIdempotencyKey(key.value()).map(PaymentRepositoryAdapter::toDomain);
    }

    @Override
    public List<Payment> findByBooking(BookingId bookingId) {
        return jpa.findByBookingIdOrderByCreatedAt(bookingId.value()).stream()
                .map(PaymentRepositoryAdapter::toDomain)
                .toList();
    }

    @Override
    public Optional<Payment> findByBookingAndStatus(BookingId bookingId, PaymentStatus status) {
        return jpa.findFirstByBookingIdAndStatus(bookingId.value(), status).map(PaymentRepositoryAdapter::toDomain);
    }

    @Override
    public boolean existsByBookingAndStatus(BookingId bookingId, PaymentStatus status) {
        return jpa.existsByBookingIdAndStatus(bookingId.value(), status);
    }

    private static PaymentEntity toNewEntity(Payment payment) {
        return new PaymentEntity(
                payment.id().value(),
                payment.bookingId().value(),
                payment.amount().amount(),
                payment.amount().currency().getCurrencyCode(),
                payment.method(),
                payment.maskedInstrument(),
                payment.idempotencyKey().value(),
                payment.requestFingerprint(),
                payment.createdAt());
    }

    private static Payment toDomain(PaymentEntity entity) {
        Currency currency = Currency.getInstance(entity.getCurrency());
        return Payment.restore(
                new PaymentId(entity.getId()),
                new BookingId(entity.getBookingId()),
                Money.of(entity.getAmount(), currency),
                entity.getMethod(),
                entity.getMaskedInstrument(),
                new IdempotencyKey(entity.getIdempotencyKey()),
                entity.getRequestFingerprint(),
                entity.getCreatedAt(),
                entity.getStatus(),
                entity.getGatewayReference(),
                entity.getFailureReason(),
                entity.getRefundAmount() == null ? null : Money.of(entity.getRefundAmount(), currency),
                entity.getRefundReference());
    }
}
