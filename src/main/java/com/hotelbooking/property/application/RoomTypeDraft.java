package com.hotelbooking.property.application;

import com.hotelbooking.common.domain.Money;
import com.hotelbooking.property.domain.RoomType;

public record RoomTypeDraft(String name, int maxOccupancy, int totalRooms, Money basePricePerNight) {

    RoomType toRoomType() {
        return RoomType.create(name, maxOccupancy, totalRooms, basePricePerNight);
    }
}
