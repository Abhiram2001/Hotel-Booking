package com.hotelbooking.property.api.dto;

import com.hotelbooking.common.domain.ContactDetails;
import com.hotelbooking.property.application.OnboardOwnerCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

/** A standalone hotel sends one entry in {@code properties}; a chain sends several. */
public record OnboardOwnerRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "must be 10 to 15 digits") String phone,
        @NotEmpty List<@Valid @NotNull PropertyRequest> properties) {

    public OnboardOwnerCommand toCommand() {
        return new OnboardOwnerCommand(name, new ContactDetails(email, phone),
                properties.stream().map(PropertyRequest::toDraft).toList());
    }
}
