package com.hotelbooking.booking.api.dto;

import com.hotelbooking.booking.application.CreateBookingCommand;
import com.hotelbooking.booking.domain.GuestDetails;
import com.hotelbooking.common.domain.ContactDetails;
import com.hotelbooking.common.domain.DateRange;
import com.hotelbooking.common.domain.Occupancy;
import com.hotelbooking.property.domain.PropertyId;
import com.hotelbooking.property.domain.RoomTypeId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record CreateBookingRequest(
        @NotNull UUID propertyId,
        @NotNull UUID roomTypeId,
        @NotNull LocalDate checkIn,
        @NotNull LocalDate checkOut,
        @NotNull @Min(1) Integer guests,
        @Min(1) Integer rooms,
        @NotNull @Valid Guest guest) {

    public CreateBookingCommand toCommand() {
        return new CreateBookingCommand(
                new PropertyId(propertyId),
                new RoomTypeId(roomTypeId),
                DateRange.of(checkIn, checkOut),
                Occupancy.of(guests, Objects.requireNonNullElse(rooms, 1)),
                guest.toDomain());
    }

    public record Guest(
            @NotBlank String fullName,
            @NotBlank @Email String email,
            @NotBlank @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "must be 10 to 15 digits") String phone) {

        GuestDetails toDomain() {
            return new GuestDetails(fullName, new ContactDetails(email, phone));
        }
    }
}
