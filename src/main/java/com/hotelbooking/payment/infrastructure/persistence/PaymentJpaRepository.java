package com.hotelbooking.payment.infrastructure.persistence;

import com.hotelbooking.payment.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface PaymentJpaRepository extends JpaRepository<PaymentEntity, UUID> {

    Optional<PaymentEntity> findByIdempotencyKey(String idempotencyKey);

    List<PaymentEntity> findByBookingIdOrderByCreatedAt(UUID bookingId);

    Optional<PaymentEntity> findFirstByBookingIdAndStatus(UUID bookingId, PaymentStatus status);

    boolean existsByBookingIdAndStatus(UUID bookingId, PaymentStatus status);
}
