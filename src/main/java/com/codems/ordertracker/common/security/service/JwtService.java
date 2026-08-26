package com.codems.ordertracker.common.security.service;

import com.codems.ordertracker.common.config.properties.JwtProperties;
import com.codems.ordertracker.common.security.model.SecurityUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    public String generateAccessToken(SecurityUser user) {
        Instant now = Instant.now();
        Instant expiresAt = expiresAt(now);

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getUserId())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey())
                .compact();
    }

    public Instant expiresAt() {
        return expiresAt(Instant.now());
    }

    public String extractSubject(String token) {
        return claims(token).getSubject();
    }

    public boolean isValid(String token) {
        try {
            claims(token);
            return true;
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    private Claims claims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Instant expiresAt(Instant issuedAt) {
        return issuedAt.plusMillis(jwtProperties.expiration());
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }
}
