package com.hotelbooking.booking.application;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.LocalTime;

/**
 * @param holdDuration        how long rooms stay reserved for an unpaid booking
 * @param maxNights           longest stay allowed in one booking
 * @param maxRoomsPerBooking  most rooms allowed in one booking
 * @param checkInTime         standard check-in time; cancellation notice is measured up to it
 */
@Validated
@ConfigurationProperties("hotel.booking")
public record BookingProperties(
        @DefaultValue("PT15M") @NotNull Duration holdDuration,
        @DefaultValue("30") @Min(1) int maxNights,
        @DefaultValue("5") @Min(1) int maxRoomsPerBooking,
        @DefaultValue("14:00") @DateTimeFormat(pattern = "HH:mm") @NotNull LocalTime checkInTime) {
}
