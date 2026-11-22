package com.hotelbooking.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Every time-dependent rule (holds, cancellation windows, "today") reads this clock, so tests can control time.
 * The clock carries the hotel time zone, so {@code LocalDate.now(clock)} is the local date at the properties.
 */
@Configuration
class TimeConfiguration {

    @Bean
    Clock clock(@Value("${hotel.time-zone:Asia/Kolkata}") ZoneId zone) {
        return Clock.system(zone);
    }
}
