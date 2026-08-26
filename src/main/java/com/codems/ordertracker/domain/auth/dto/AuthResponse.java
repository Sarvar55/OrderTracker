package com.codems.ordertracker.domain.auth.dto;

import com.codems.ordertracker.domain.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Authentication token response")
public record AuthResponse(
        @Schema(example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(example = "JWT")
        String tokenType,

        @Schema(example = "2026-07-15T10:00:00Z")
        Instant expiresAt,

        @Schema(description = "Authenticated user information")
        UserResponse user

) {
    public static AuthResponse of(String accessToken, Instant expiresAt, UserResponse user) {
        return new AuthResponse(accessToken, "JWT", expiresAt, user);
    }
}
