package com.codems.ordertracker.domain.auth.dto;

import com.codems.ordertracker.common.validation.CompromisedPassword;
import com.codems.ordertracker.common.validation.UniqueEmail;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for user registration")
public record RegisterRequest(
        @Schema(example = "sarvar")
        @NotBlank(message = "Username is required")
        @Size(max = 100, message = "Username must be at most 100 characters")
        String username,

        @Schema(example = "sarvar@example.com")
        @NotBlank(message = "Email is required")
        @UniqueEmail(message = "Email already exists")
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must be at most 255 characters")
        String email,

        @Schema(example = "StrongPassword123!")
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        @CompromisedPassword(message = "Password has been compromised")
        String password
) {
    @Override
    public String toString() {
        return "RegisterRequest[username=" + username + ", email=" + email + ", password=***]";
    }
}
