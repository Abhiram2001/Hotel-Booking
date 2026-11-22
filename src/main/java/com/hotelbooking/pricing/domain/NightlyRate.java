package com.hotelbooking.pricing.domain;

import com.hotelbooking.common.domain.Money;

import java.time.LocalDate;

public record NightlyRate(LocalDate date, Money pricePerRoom) {
}
