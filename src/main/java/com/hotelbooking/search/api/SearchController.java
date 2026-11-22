package com.hotelbooking.search.api;

import com.hotelbooking.search.application.PropertySearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Discovery")
class SearchController {

    private final PropertySearchService search;

    SearchController(PropertySearchService search) {
        this.search = search;
    }

    @GetMapping("/api/v1/properties/search")
    @Operation(summary = "Search available properties",
            description = "Returns properties in the city with at least one room type available for every night "
                    + "of the stay, sorted by lowest total price.")
    SearchResponse search(@Valid @ParameterObject @ModelAttribute SearchRequest request) {
        return SearchResponse.from(search.search(request.toCriteria()));
    }
}
