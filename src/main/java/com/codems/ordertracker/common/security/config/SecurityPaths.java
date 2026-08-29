package com.codems.ordertracker.common.security.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SecurityPaths {

    @Bean("publicPaths")
    public List<String> publicPaths() {
        return List.of(
                "/api/auth/register",
                "/api/auth/login",
                "/api/webhooks/pgayment",
                "/api/webhooks/shipment",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/swagger-ui.html"
        );
    }
}