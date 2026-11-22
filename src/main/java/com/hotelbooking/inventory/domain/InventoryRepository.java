package com.hotelbooking.inventory.domain;

import com.hotelbooking.common.domain.DateRange;
import com.hotelbooking.property.domain.RoomTypeId;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface InventoryRepository {

    void createAll(Collection<RoomNightInventory> nights);

    /**
     * Loads the nights of a stay and locks them for the current transaction, in ascending date order.
     * A consistent lock order means two overlapping bookings cannot deadlock each other.
     */
    List<RoomNightInventory> lockNights(RoomTypeId roomTypeId, DateRange stay);

    void saveAll(Collection<RoomNightInventory> nights);

    /**
     * Lowest number of free rooms across all nights of the stay, per room type. Room types that do not have
     * inventory for every night are left out.
     */
    Map<RoomTypeId, Integer> findAvailableRooms(Collection<RoomTypeId> roomTypeIds, DateRange stay);
}
