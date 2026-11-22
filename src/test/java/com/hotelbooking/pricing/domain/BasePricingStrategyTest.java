package com.hotelbooking.pricing.domain;

import com.hotelbooking.common.domain.DateRange;
import com.hotelbooking.common.domain.Money;
import com.hotelbooking.property.domain.Amenity;
import com.hotelbooking.property.domain.CancellationPolicyType;
import com.hotelbooking.property.domain.Location;
import com.hotelbooking.property.domain.OwnerId;
import com.hotelbooking.property.domain.Property;
import com.hotelbooking.property.domain.RoomType;
import com.hotelbooking.property.domain.StarRating;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BasePricingStrategyTest {

    @Test
    void chargesTheBaseRateForEveryNightAndRoom() {
        RoomType roomType = RoomType.create("Deluxe", 2, 5, Money.inr(2500));
        Property property = Property.onboard(OwnerId.newId(), "Lemon Leaf", new Location("1 MG Road", "Central", "Pune"),
                StarRating.of(3), Set.of(Amenity.WIFI), List.of(roomType), CancellationPolicyType.FLEXIBLE);
        DateRange stay = DateRange.of(LocalDate.of(2030, 6, 10), LocalDate.of(2030, 6, 13));

        PriceQuote quote = new BasePricingStrategy().quote(new PricingRequest(property, roomType, stay, 2));

        assertThat(quote.nightlyRates()).extracting(NightlyRate::date).containsExactlyElementsOf(stay.stayDates());
        assertThat(quote.averageNightlyRate()).isEqualTo(Money.inr(2500));
        assertThat(quote.pricePerRoomForStay()).isEqualTo(Money.inr(7500));
        assertThat(quote.total()).isEqualTo(Money.inr(15000));
    }
}
