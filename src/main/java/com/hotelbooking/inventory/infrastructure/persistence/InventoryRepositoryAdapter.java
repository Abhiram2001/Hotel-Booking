package com.hotelbooking.inventory.infrastructure.persistence;

import com.hotelbooking.common.domain.DateRange;
import com.hotelbooking.inventory.domain.InventoryRepository;
import com.hotelbooking.inventory.domain.RoomNightInventory;
import com.hotelbooking.property.domain.RoomTypeId;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
class InventoryRepositoryAdapter implements InventoryRepository {

    private final RoomNightJpaRepository jpa;

    InventoryRepositoryAdapter(RoomNightJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void createAll(Collection<RoomNightInventory> nights) {
        jpa.saveAll(nights.stream()
                .map(night -> new RoomNightEntity(keyOf(night), night.totalRooms(), night.bookedRooms()))
                .toList());
    }

    @Override
    public List<RoomNightInventory> lockNights(RoomTypeId roomTypeId, DateRange stay) {
        return jpa.findForUpdate(roomTypeId.value(), stay.checkIn(), stay.checkOut()).stream()
                .map(InventoryRepositoryAdapter::toDomain)
                .toList();
    }

    /** The rows were loaded (and locked) earlier in this transaction, so findById hits the persistence context. */
    @Override
    public void saveAll(Collection<RoomNightInventory> nights) {
        for (RoomNightInventory night : nights) {
            RoomNightEntity entity = jpa.findById(keyOf(night)).orElseThrow(() -> new IllegalStateException(
                    "Inventory row missing for %s on %s".formatted(night.roomTypeId(), night.date())));
            entity.setBookedRooms(night.bookedRooms());
        }
    }

    @Override
    public Map<RoomTypeId, Integer> findAvailableRooms(Collection<RoomTypeId> roomTypeIds, DateRange stay) {
        List<UUID> ids = roomTypeIds.stream().map(RoomTypeId::value).toList();
        return jpa.findAvailability(ids, stay.checkIn(), stay.checkOut()).stream()
                .filter(row -> row.getNights().intValue() == stay.nights())
                .collect(Collectors.toMap(
                        row -> new RoomTypeId(row.getRoomTypeId()),
                        row -> row.getAvailableRooms().intValue()));
    }

    private static RoomNightKey keyOf(RoomNightInventory night) {
        return new RoomNightKey(night.roomTypeId().value(), night.date());
    }

    private static RoomNightInventory toDomain(RoomNightEntity entity) {
        return new RoomNightInventory(new RoomTypeId(entity.getId().getRoomTypeId()), entity.getId().getStayDate(),
                entity.getTotalRooms(), entity.getBookedRooms());
    }
}
