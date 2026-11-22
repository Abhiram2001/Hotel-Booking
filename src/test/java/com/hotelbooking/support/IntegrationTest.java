package com.hotelbooking.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Every integration test uses this exact configuration, so Spring builds one context (and one H2 database)
 * for the whole run. Tests isolate their data by onboarding their own properties in their own city.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@SpringBootTest(properties = {
        "hotel.demo-data.enabled=false",
        "hotel.booking.expiry-job.enabled=false",
        "hotel.inventory.horizon-days=120"
})
@AutoConfigureMockMvc
@Import(IntegrationTestConfiguration.class)
public @interface IntegrationTest {
}
