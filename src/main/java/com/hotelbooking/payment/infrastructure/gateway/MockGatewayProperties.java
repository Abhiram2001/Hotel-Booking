package com.hotelbooking.payment.infrastructure.gateway;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.Set;

/** @param declinedInstruments instrument tokens the mock gateway always declines */
@ConfigurationProperties("hotel.payment.mock")
public record MockGatewayProperties(@DefaultValue Set<String> declinedInstruments) {
}
