package com.helpdesk.backend.shared.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtProvider {

    private final SecretKey key;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-expiration:3600000}") long accessTokenExpiration,
            @Value("${app.jwt.refresh-expiration:604800000}") long refreshTokenExpiration) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException(
                    "app.jwt.secret must be at least 32 characters. Current length: "
                            + (secret == null ? 0 : secret.length()));
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String generateAccessToken(UUID userId, String email, String role) {
        return buildToken(userId, email, role, accessTokenExpiration);
    }

    public String generateRefreshToken(UUID userId, String email, String role) {
        return buildToken(userId, email, role, refreshTokenExpiration);
    }

    private String buildToken(UUID userId, String email, String role, long expiration) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
