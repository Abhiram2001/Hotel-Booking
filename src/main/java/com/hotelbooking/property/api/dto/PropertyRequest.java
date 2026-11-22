package com.hotelbooking.property.api.dto;

import com.hotelbooking.property.application.PropertyDraft;
import com.hotelbooking.property.domain.Amenity;
import com.hotelbooking.property.domain.CancellationPolicyType;
import com.hotelbooking.property.domain.Location;
import com.hotelbooking.property.domain.StarRating;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Set;

public record PropertyRequest(
        @NotBlank String name,
        @NotBlank String addressLine,
        @NotBlank String locality,
        @NotBlank String city,
        @NotNull @Min(1) @Max(5) Integer starRating,
        Set<Amenity> amenities,
        @NotEmpty List<@Valid @NotNull RoomTypeRequest> roomTypes,
        @NotNull CancellationPolicyType cancellationPolicy) {

    public PropertyDraft toDraft() {
        return new PropertyDraft(
                name,
                new Location(addressLine, locality, city),
                StarRating.of(starRating),
                amenities == null ? Set.of() : amenities,
                roomTypes.stream().map(RoomTypeRequest::toDraft).toList(),
                cancellationPolicy);
    }
}
