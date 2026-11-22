package com.hotelbooking.inventory.application;

import com.hotelbooking.common.domain.DateRange;
import com.hotelbooking.common.domain.error.BusinessRuleViolationException;
import com.hotelbooking.inventory.domain.InventoryRepository;
import com.hotelbooking.inventory.domain.RoomNightInventory;
import com.hotelbooking.property.domain.RoomTypeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final InventoryRepository repository;
    private final InventoryProperties properties;
    private final Clock clock;

    public InventoryService(InventoryRepository repository, InventoryProperties properties, Clock clock) {
        this.repository = repository;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional
    public void openInventory(RoomTypeId roomTypeId, int totalRooms) {
        LocalDate today = LocalDate.now(clock);
        List<RoomNightInventory> nights = today.datesUntil(today.plusDays(properties.horizonDays()))
                .map(date -> RoomNightInventory.open(roomTypeId, date, totalRooms))
                .toList();
        repository.createAll(nights);
        log.debug("Opened {} night(s) from {} for room type {} with {} room(s) each", nights.size(), today,
                roomTypeId, totalRooms);
    }

    /**
     * Holds rooms for every night of the stay, or for none. Must join the caller's transaction so the row
     * locks are held until the booking that owns the rooms is committed.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void reserve(RoomTypeId roomTypeId, DateRange stay, int rooms) {
        List<RoomNightInventory> nights = lockWholeStay(roomTypeId, stay);
        nights.forEach(night -> night.reserve(rooms));
        repository.saveAll(nights);
        log.debug("Reserved {} room(s) of room type {} from {} to {}", rooms, roomTypeId, stay.checkIn(), stay.checkOut());
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void release(RoomTypeId roomTypeId, DateRange stay, int rooms) {
        List<RoomNightInventory> nights = lockWholeStay(roomTypeId, stay);
        nights.forEach(night -> night.release(rooms));
        repository.saveAll(nights);
        log.debug("Released {} room(s) of room type {} from {} to {}", rooms, roomTypeId, stay.checkIn(), stay.checkOut());
    }

    @Transactional(readOnly = true)
    public Map<RoomTypeId, Integer> availableRooms(Collection<RoomTypeId> roomTypeIds, DateRange stay) {
        return roomTypeIds.isEmpty() ? Map.of() : repository.findAvailableRooms(roomTypeIds, stay);
    }

    private List<RoomNightInventory> lockWholeStay(RoomTypeId roomTypeId, DateRange stay) {
        List<RoomNightInventory> nights = repository.lockNights(roomTypeId, stay);
        if (nights.size() != stay.nights()) {
            throw new BusinessRuleViolationException("DATES_NOT_OPEN",
                    "Room type %s is not open for booking on every night from %s to %s"
                            .formatted(roomTypeId, stay.checkIn(), stay.checkOut()));
        }
        return nights;
    }
}
