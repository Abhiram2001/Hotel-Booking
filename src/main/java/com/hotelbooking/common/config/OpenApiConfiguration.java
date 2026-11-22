package com.hotelbooking.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Swagger UI at /swagger-ui.html, spec at /v3/api-docs. */
@Configuration
class OpenApiConfiguration {

    @Bean
    OpenAPI hotelBookingOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Hotel Booking API")
                .version("v1")
                .description("Discover properties, onboard owners, book rooms, pay and cancel. "
                        + "Errors are RFC 9457 problem details with a machine-readable 'code'."));
    }
}
