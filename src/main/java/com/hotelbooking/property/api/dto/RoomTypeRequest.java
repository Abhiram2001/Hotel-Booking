package com.hotelbooking.property.api.dto;

import com.hotelbooking.common.domain.Money;
import com.hotelbooking.property.application.RoomTypeDraft;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RoomTypeRequest(
        @NotBlank String name,
        @NotNull @Min(1) @Max(20) Integer maxOccupancy,
        @NotNull @Min(1) @Max(1000) Integer totalRooms,
        @NotNull @DecimalMin("1.00") @Digits(integer = 10, fraction = 2) BigDecimal basePricePerNight) {

    public RoomTypeDraft toDraft() {
        return new RoomTypeDraft(name, maxOccupancy, totalRooms, Money.inr(basePricePerNight));
    }
}
