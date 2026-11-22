package com.hotelbooking.pricing.domain;

import com.hotelbooking.common.domain.Money;

/** Charges the room type's base rate for every night. */
public final class BasePricingStrategy implements PricingStrategy {

    @Override
    public PriceQuote quote(PricingRequest request) {
        Money rate = request.roomType().basePricePerNight();
        return new PriceQuote(
                request.stay().stayDates().stream().map(date -> new NightlyRate(date, rate)).toList(),
                request.rooms());
    }
}
