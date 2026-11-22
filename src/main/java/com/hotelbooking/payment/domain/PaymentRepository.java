package com.hotelbooking.payment.domain;

import com.hotelbooking.booking.domain.BookingId;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(PaymentId id);

    Optional<Payment> findByIdempotencyKey(IdempotencyKey key);

    List<Payment> findByBooking(BookingId bookingId);

    Optional<Payment> findByBookingAndStatus(BookingId bookingId, PaymentStatus status);

    boolean existsByBookingAndStatus(BookingId bookingId, PaymentStatus status);
}
