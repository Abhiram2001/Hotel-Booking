package com.hotelbooking.search.application;

import com.hotelbooking.inventory.application.InventoryService;
import com.hotelbooking.pricing.domain.PricingRequest;
import com.hotelbooking.pricing.domain.PricingStrategy;
import com.hotelbooking.property.application.PropertyQueryService;
import com.hotelbooking.property.domain.Property;
import com.hotelbooking.property.domain.RoomType;
import com.hotelbooking.property.domain.RoomTypeId;
import com.hotelbooking.search.application.filter.SearchFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Finds properties in a city with at least one room type that is available, fits the guests and passes every
 * requested filter. Availability for all candidate room types is loaded in one query.
 */
@Service
@Transactional(readOnly = true)
public class PropertySearchService {

    private static final Logger log = LoggerFactory.getLogger(PropertySearchService.class);

    private final PropertyQueryService properties;
    private final InventoryService inventory;
    private final PricingStrategy pricing;
    private final List<SearchFilter> filters;
    private final Clock clock;

    public PropertySearchService(PropertyQueryService properties, InventoryService inventory, PricingStrategy pricing,
                                 List<SearchFilter> filters, Clock clock) {
        this.properties = properties;
        this.inventory = inventory;
        this.pricing = pricing;
        this.filters = List.copyOf(filters);
        this.clock = clock;
    }

    public List<PropertySearchResult> search(SearchCriteria criteria) {
        criteria.stay().ensureNotInPast(LocalDate.now(clock));
        List<Property> candidates = properties.findInCity(criteria.city());
        Map<RoomTypeId, Integer> availability = inventory.availableRooms(roomTypeIds(candidates), criteria.stay());
        List<SearchFilter> activeFilters = filters.stream().filter(filter -> filter.isActive(criteria)).toList();

        List<PropertySearchResult> results = candidates.stream()
                .map(property -> new PropertySearchResult(property,
                        matchingOffers(property, availability, criteria, activeFilters)))
                .filter(result -> !result.offers().isEmpty())
                .sorted(Comparator.comparing(PropertySearchResult::lowestTotal))
                .toList();
        log.debug("Search in {} from {} to {} for {} guest(s) in {} room(s) with filters {}: {} of {} properties match",
                criteria.city(), criteria.stay().checkIn(), criteria.stay().checkOut(), criteria.occupancy().guests(),
                criteria.occupancy().rooms(), filterNames(activeFilters), results.size(), candidates.size());
        return results;
    }

    private static List<String> filterNames(List<SearchFilter> activeFilters) {
        return activeFilters.stream().map(filter -> filter.getClass().getSimpleName()).toList();
    }

    private List<RoomOffer> matchingOffers(Property property, Map<RoomTypeId, Integer> availability,
                                           SearchCriteria criteria, List<SearchFilter> activeFilters) {
        return property.roomTypes().stream()
                .map(roomType -> offerFor(property, roomType, availability, criteria))
                .filter(offer -> activeFilters.stream().allMatch(filter -> filter.matches(offer, criteria)))
                .toList();
    }

    private RoomOffer offerFor(Property property, RoomType roomType, Map<RoomTypeId, Integer> availability,
                               SearchCriteria criteria) {
        int rooms = criteria.occupancy().rooms();
        return new RoomOffer(property, roomType, availability.getOrDefault(roomType.id(), 0),
                pricing.quote(new PricingRequest(property, roomType, criteria.stay(), rooms)));
    }

    private static List<RoomTypeId> roomTypeIds(List<Property> properties) {
        return properties.stream().flatMap(property -> property.roomTypes().stream()).map(RoomType::id).toList();
    }
}
