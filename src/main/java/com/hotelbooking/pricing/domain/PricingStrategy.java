package com.hotelbooking.pricing.domain;

/**
 * Prices a stay. Search and booking both depend only on this interface, so dynamic pricing (weekend
 * surcharges, occupancy-based rates, seasonal rules) can replace or wrap the base strategy without touching them.
 */
public interface PricingStrategy {

    PriceQuote quote(PricingRequest request);
}
