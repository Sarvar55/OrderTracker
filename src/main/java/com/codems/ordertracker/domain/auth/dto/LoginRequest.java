package com.codems.ordertracker.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request body for user login")
public record LoginRequest(
        @Schema(example = "sarvar@example.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @Schema(example = "StrongPassword123!")
        @NotBlank(message = "Password is required")
        String password
) {
    @Override
    public String toString() {
        return "LoginRequest[email=" + email + ", password=***]";
    }
}
