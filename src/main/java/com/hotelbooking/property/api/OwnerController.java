package com.hotelbooking.property.api;

import com.hotelbooking.property.api.dto.OnboardOwnerRequest;
import com.hotelbooking.property.api.dto.OwnerResponse;
import com.hotelbooking.property.api.dto.PropertyRequest;
import com.hotelbooking.property.api.dto.PropertyResponse;
import com.hotelbooking.property.application.OwnerPortfolio;
import com.hotelbooking.property.application.PropertyOnboardingService;
import com.hotelbooking.property.application.PropertyQueryService;
import com.hotelbooking.property.domain.OwnerId;
import com.hotelbooking.property.domain.Property;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/owners")
@Tag(name = "Onboarding", description = "Owners (standalone or chain) and their properties")
class OwnerController {

    private final PropertyOnboardingService onboarding;
    private final PropertyQueryService queries;

    OwnerController(PropertyOnboardingService onboarding, PropertyQueryService queries) {
        this.onboarding = onboarding;
        this.queries = queries;
    }

    @PostMapping
    @Operation(summary = "Onboard an owner with one or more properties")
    ResponseEntity<OwnerResponse> onboardOwner(@Valid @RequestBody OnboardOwnerRequest request) {
        OwnerPortfolio portfolio = onboarding.onboardOwner(request.toCommand());
        return ResponseEntity.created(URI.create("/api/v1/owners/" + portfolio.owner().id()))
                .body(OwnerResponse.from(portfolio));
    }

    @PostMapping("/{ownerId}/properties")
    @Operation(summary = "Add a property to an existing owner")
    ResponseEntity<PropertyResponse> addProperty(@PathVariable UUID ownerId, @Valid @RequestBody PropertyRequest request) {
        Property property = onboarding.addProperty(new OwnerId(ownerId), request.toDraft());
        return ResponseEntity.created(URI.create("/api/v1/properties/" + property.id()))
                .body(PropertyResponse.from(property));
    }

    @GetMapping("/{ownerId}")
    @Operation(summary = "Get an owner and all of its properties")
    OwnerResponse getOwner(@PathVariable UUID ownerId) {
        return OwnerResponse.from(queries.getPortfolio(new OwnerId(ownerId)));
    }
}
