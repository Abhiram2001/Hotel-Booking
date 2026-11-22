package com.hotelbooking.inventory.application;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/** @param horizonDays how many nights ahead inventory is opened when a room type is onboarded */
@Validated
@ConfigurationProperties("hotel.inventory")
public record InventoryProperties(@DefaultValue("365") @Min(1) @Max(730) int horizonDays) {
}
