package com.hotelbooking.bootstrap;

import com.hotelbooking.common.domain.ContactDetails;
import com.hotelbooking.common.domain.Money;
import com.hotelbooking.property.application.OnboardOwnerCommand;
import com.hotelbooking.property.application.OwnerPortfolio;
import com.hotelbooking.property.application.PropertyDraft;
import com.hotelbooking.property.application.PropertyOnboardingService;
import com.hotelbooking.property.application.RoomTypeDraft;
import com.hotelbooking.property.domain.CancellationPolicyType;
import com.hotelbooking.property.domain.Location;
import com.hotelbooking.property.domain.StarRating;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static com.hotelbooking.property.domain.Amenity.AIR_CONDITIONING;
import static com.hotelbooking.property.domain.Amenity.BREAKFAST_INCLUDED;
import static com.hotelbooking.property.domain.Amenity.GYM;
import static com.hotelbooking.property.domain.Amenity.PARKING;
import static com.hotelbooking.property.domain.Amenity.RESTAURANT;
import static com.hotelbooking.property.domain.Amenity.SWIMMING_POOL;
import static com.hotelbooking.property.domain.Amenity.WIFI;

/**
 * Seeds one hotel chain (three properties) and one standalone homestay through the normal onboarding flow,
 * so the API can be tried right after startup. Disable with {@code hotel.demo-data.enabled=false}.
 */
@Component
@ConditionalOnProperty(name = "hotel.demo-data.enabled", havingValue = "true")
class DemoDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataLoader.class);

    private final PropertyOnboardingService onboarding;

    DemoDataLoader(PropertyOnboardingService onboarding) {
        this.onboarding = onboarding;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<OwnerPortfolio> owners = List.of(onboarding.onboardOwner(chain()), onboarding.onboardOwner(standalone()));
        owners.forEach(portfolio -> log.info("Demo owner '{}' ({}) with {} propert(ies)",
                portfolio.owner().name(), portfolio.owner().id(), portfolio.properties().size()));
    }

    private static OnboardOwnerCommand chain() {
        return new OnboardOwnerCommand("Lemon Leaf Hotels", new ContactDetails("ops@lemonleaf.example", "+918041234567"),
                List.of(
                        new PropertyDraft("Lemon Leaf Premier", new Location("80 Feet Road", "Koramangala", "Bengaluru"),
                                StarRating.of(4), Set.of(WIFI, SWIMMING_POOL, GYM, RESTAURANT, PARKING),
                                List.of(room("Deluxe", 2, 10, 4500), room("Suite", 4, 3, 9000)),
                                CancellationPolicyType.MODERATE),
                        new PropertyDraft("Lemon Leaf Express", new Location("100 Feet Road", "Indiranagar", "Bengaluru"),
                                StarRating.of(3), Set.of(WIFI, BREAKFAST_INCLUDED, PARKING),
                                List.of(room("Standard", 2, 15, 2800), room("Family", 4, 5, 5200)),
                                CancellationPolicyType.FLEXIBLE),
                        new PropertyDraft("Lemon Leaf Andheri", new Location("Marol Naka", "Andheri East", "Mumbai"),
                                StarRating.of(4), Set.of(WIFI, GYM, RESTAURANT, AIR_CONDITIONING),
                                List.of(room("Deluxe", 2, 12, 6000)),
                                CancellationPolicyType.MODERATE)));
    }

    private static OnboardOwnerCommand standalone() {
        return new OnboardOwnerCommand("Sea Breeze Homestay", new ContactDetails("stay@seabreeze.example", "+918322123456"),
                List.of(new PropertyDraft("Sea Breeze Homestay", new Location("Beach Road", "Calangute", "Goa"),
                        StarRating.of(3), Set.of(WIFI, BREAKFAST_INCLUDED, AIR_CONDITIONING),
                        List.of(room("Garden Room", 3, 4, 3500)),
                        CancellationPolicyType.NON_REFUNDABLE)));
    }

    private static RoomTypeDraft room(String name, int maxOccupancy, int totalRooms, long pricePerNight) {
        return new RoomTypeDraft(name, maxOccupancy, totalRooms, Money.inr(pricePerNight));
    }
}
