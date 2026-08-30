package com.codems.ordertracker.common.security.config;

import com.codems.ordertracker.common.constants.ApplicationConstants;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityPaths {

    @Bean("publicPaths")
    public List<String> publicPaths() {
        return List.of(
                ApplicationConstants.API_PREFIX + "/auth/register",
                ApplicationConstants.API_PREFIX + "/auth/login",
                ApplicationConstants.API_PREFIX + "/webhooks/payment",
                ApplicationConstants.API_PREFIX + "/webhooks/shipment",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/swagger-ui.html"
        );
    }
}
