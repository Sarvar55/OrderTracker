package com.codems.ordertracker;

import com.codems.ordertracker.common.config.properties.CorsConfigProperties;
import com.codems.ordertracker.common.config.properties.JwtProperties;
import com.codems.ordertracker.common.config.properties.WebhookProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@EnableConfigurationProperties({
        CorsConfigProperties.class,
        JwtProperties.class,
        WebhookProperties.class
})
public class OrderTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderTrackerApplication.class, args);
    }
}
