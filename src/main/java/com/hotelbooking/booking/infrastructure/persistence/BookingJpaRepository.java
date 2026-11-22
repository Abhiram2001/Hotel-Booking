package com.hotelbooking.booking.infrastructure.persistence;

import com.hotelbooking.booking.domain.BookingStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface BookingJpaRepository extends JpaRepository<BookingEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from BookingEntity b where b.id = :id")
    Optional<BookingEntity> findByIdForUpdate(UUID id);

    @Query("""
            select b.id from BookingEntity b
            where b.status = :status and b.holdExpiresAt <= :now
            order by b.holdExpiresAt""")
    List<UUID> findIdsWithExpiredHold(BookingStatus status, Instant now, Pageable page);
}
