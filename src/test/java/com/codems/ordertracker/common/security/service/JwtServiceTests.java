package com.codems.ordertracker.common.security.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.codems.ordertracker.common.config.properties.JwtProperties;
import com.codems.ordertracker.common.security.model.SecurityUser;
import com.codems.ordertracker.domain.user.entity.User;
import org.junit.jupiter.api.Test;

class JwtServiceTests {

    private final JwtService jwtService = new JwtService(
            new JwtProperties("order-tracker-test-secret-with-at-least-32-bytes", 3_600_000)
    );

    @Test
    void shouldGenerateAndValidateAccessToken() {
        User user = User.of("customer", "customer@example.com", "encoded-password");
        user.setId(7L);
        SecurityUser securityUser = SecurityUser.from(user);

        String token = jwtService.generateAccessToken(securityUser);

        assertThat(jwtService.isValid(token)).isTrue();
        assertThat(jwtService.extractSubject(token)).isEqualTo("customer@example.com");
    }

    @Test
    void shouldRejectInvalidToken() {
        assertThat(jwtService.isValid("invalid-token")).isFalse();
    }
}
