package com.codems.ordertracker.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authenticated user information")
public record UserResponse(
        @Schema(example = "1")
        Long id,

        @Schema(example = "sarvar")
        String username,

        @Schema(example = "sarvar@example.com")
        String email
) {
}
