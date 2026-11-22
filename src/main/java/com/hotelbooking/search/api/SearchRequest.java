package com.hotelbooking.search.api;

import com.hotelbooking.common.domain.DateRange;
import com.hotelbooking.common.domain.Money;
import com.hotelbooking.common.domain.Occupancy;
import com.hotelbooking.property.domain.Amenity;
import com.hotelbooking.search.application.SearchCriteria;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

/** Query parameters of the search endpoint. {@code amenities} accepts a comma-separated list. */
record SearchRequest(
        @NotBlank String city,
        String locality,
        @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
        @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
        @NotNull @Min(1) Integer guests,
        @Min(1) Integer rooms,
        @DecimalMin("0") BigDecimal minPrice,
        @DecimalMin("0") BigDecimal maxPrice,
        Set<Amenity> amenities,
        @Min(1) @Max(5) Integer minRating) {

    SearchCriteria toCriteria() {
        return new SearchCriteria(
                city,
                DateRange.of(checkIn, checkOut),
                Occupancy.of(guests, Objects.requireNonNullElse(rooms, 1)),
                locality,
                minPrice == null ? null : Money.inr(minPrice),
                maxPrice == null ? null : Money.inr(maxPrice),
                amenities,
                minRating);
    }
}
