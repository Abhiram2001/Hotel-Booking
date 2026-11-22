package com.hotelbooking.property.domain;

import com.hotelbooking.common.domain.Guard;

public record StarRating(int stars) {

    public static final int MIN = 1;
    public static final int MAX = 5;

    public StarRating {
        Guard.isTrue(stars >= MIN && stars <= MAX, "Star rating must be between %d and %d".formatted(MIN, MAX));
    }

    public static StarRating of(int stars) {
        return new StarRating(stars);
    }

    public boolean isAtLeast(int minimumStars) {
        return stars >= minimumStars;
    }
}
