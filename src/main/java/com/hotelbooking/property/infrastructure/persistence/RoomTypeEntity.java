package com.hotelbooking.property.infrastructure.persistence;

import com.hotelbooking.common.infrastructure.persistence.AssignedIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "room_type")
class RoomTypeEntity extends AssignedIdEntity<UUID> {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "max_occupancy", nullable = false)
    private int maxOccupancy;

    @Column(name = "total_rooms", nullable = false)
    private int totalRooms;

    @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Column(nullable = false, length = 3)
    private String currency;

    protected RoomTypeEntity() {
    }

    RoomTypeEntity(UUID id, String name, int maxOccupancy, int totalRooms, BigDecimal basePrice, String currency) {
        this.id = id;
        this.name = name;
        this.maxOccupancy = maxOccupancy;
        this.totalRooms = totalRooms;
        this.basePrice = basePrice;
        this.currency = currency;
    }

    @Override
    public UUID getId() {
        return id;
    }

    String getName() {
        return name;
    }

    int getMaxOccupancy() {
        return maxOccupancy;
    }

    int getTotalRooms() {
        return totalRooms;
    }

    BigDecimal getBasePrice() {
        return basePrice;
    }

    String getCurrency() {
        return currency;
    }
}
