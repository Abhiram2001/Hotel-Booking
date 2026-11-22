package com.hotelbooking.inventory.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

interface RoomNightJpaRepository extends JpaRepository<RoomNightEntity, RoomNightKey> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select n from RoomNightEntity n
            where n.id.roomTypeId = :roomTypeId and n.id.stayDate >= :from and n.id.stayDate < :to
            order by n.id.stayDate""")
    List<RoomNightEntity> findForUpdate(UUID roomTypeId, LocalDate from, LocalDate to);

    @Query("""
            select n.id.roomTypeId as roomTypeId,
                   min(n.totalRooms - n.bookedRooms) as availableRooms,
                   count(n) as nights
            from RoomNightEntity n
            where n.id.roomTypeId in :roomTypeIds and n.id.stayDate >= :from and n.id.stayDate < :to
            group by n.id.roomTypeId""")
    List<AvailabilityRow> findAvailability(Collection<UUID> roomTypeIds, LocalDate from, LocalDate to);

    interface AvailabilityRow {

        UUID getRoomTypeId();

        Number getAvailableRooms();

        Number getNights();
    }
}
