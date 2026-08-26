package com.codems.ordertracker.common.config.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.webhook")
public record WebhookProperties(@NotBlank String secret) {
}
