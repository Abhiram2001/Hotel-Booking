package com.hotelbooking.property.api;

import com.hotelbooking.property.api.dto.PropertyResponse;
import com.hotelbooking.property.application.PropertyQueryService;
import com.hotelbooking.property.domain.PropertyId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/properties")
@Tag(name = "Properties")
class PropertyController {

    private final PropertyQueryService queries;

    PropertyController(PropertyQueryService queries) {
        this.queries = queries;
    }

    @GetMapping("/{propertyId}")
    @Operation(summary = "Get a property and its room types")
    PropertyResponse getProperty(@PathVariable UUID propertyId) {
        return PropertyResponse.from(queries.getProperty(new PropertyId(propertyId)));
    }
}
