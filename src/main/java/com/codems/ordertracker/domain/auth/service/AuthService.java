package com.codems.ordertracker.domain.auth.service;

import com.codems.ordertracker.common.security.service.JwtService;
import com.codems.ordertracker.common.security.model.SecurityUser;
import com.codems.ordertracker.domain.auth.dto.AuthResponse;
import com.codems.ordertracker.domain.auth.dto.LoginRequest;
import com.codems.ordertracker.domain.auth.dto.RegisterRequest;
import com.codems.ordertracker.domain.user.entity.User;
import com.codems.ordertracker.domain.user.dto.UserResponse;
import com.codems.ordertracker.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public void register(RegisterRequest request) {
        User user = User.of(request.username(), request.email(), passwordEncoder.encode(request.password()));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        SecurityUser principal = (SecurityUser) authentication.getPrincipal();
        return token(principal);
    }

    private AuthResponse token(SecurityUser user) {
        return AuthResponse.of(
                jwtService.generateAccessToken(user),
                jwtService.expiresAt(),
                new UserResponse(
                        user.getUserId(),
                        user.getDisplayName(),
                        user.getEmail()
                )
        );
    }
}
