package com.hotelbooking.pricing.domain;

import com.hotelbooking.common.domain.Guard;
import com.hotelbooking.common.domain.Money;

import java.util.List;

/** A per-night price breakdown, so strategies can price each night differently. */
public record PriceQuote(List<NightlyRate> nightlyRates, int rooms) {

    public PriceQuote {
        Guard.notNull(nightlyRates, "nightlyRates");
        Guard.isTrue(!nightlyRates.isEmpty(), "A quote needs at least one night");
        Guard.positive(rooms, "rooms");
        nightlyRates = List.copyOf(nightlyRates);
    }

    public Money pricePerRoomForStay() {
        return nightlyRates.stream().map(NightlyRate::pricePerRoom).reduce(Money::plus).orElseThrow();
    }

    public Money averageNightlyRate() {
        return pricePerRoomForStay().dividedBy(nightlyRates.size());
    }

    public Money total() {
        return pricePerRoomForStay().times(rooms);
    }
}
