package com.codems.ordertracker.common.config.properties;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.cors")
public record CorsConfigProperties(
        @NotEmpty String[] allowedOrigins,
        @NotEmpty String[] allowedMethods,
        @NotEmpty String[] allowedHeaders,
        boolean allowCredentials,
        @PositiveOrZero Long maxAge
) {
}
