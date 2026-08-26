package com.codems.ordertracker.domain.auth.controller;

import com.codems.ordertracker.common.constants.ApplicationConstants;
import com.codems.ordertracker.domain.auth.dto.AuthResponse;
import com.codems.ordertracker.domain.auth.dto.LoginRequest;
import com.codems.ordertracker.domain.auth.dto.RegisterRequest;
import com.codems.ordertracker.domain.auth.service.AuthService;
import com.codems.ordertracker.domain.base.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register and login operations")
public class AuthController {

    private final AuthService authService;

    @PostMapping(value = "/register", version = ApplicationConstants.DEFAULT_API_VERSION)
    @Operation(
            summary = "Register a user",
            description = "Creates a new user account with the supplied username, email address, and password."
    )
    public ResponseEntity<BaseResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(null, HttpStatus.CREATED, "Registered successfully"));
    }

    @PostMapping(value = "/login", version = ApplicationConstants.DEFAULT_API_VERSION)
    @Operation(
            summary = "Log in",
            description = "Checks the supplied email address and password, then returns a JWT access token and "
                    + "basic information about the authenticated user."
    )
    public ResponseEntity<BaseResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(BaseResponse.success(authService.login(request)));
    }
}
