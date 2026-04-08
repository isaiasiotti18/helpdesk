package com.helpdesk.backend.modules.auth;

import com.helpdesk.backend.modules.auth.domain.RefreshToken;
import com.helpdesk.backend.modules.auth.domain.RefreshTokenRepository;
import com.helpdesk.backend.modules.auth.dtos.AuthResponse;
import com.helpdesk.backend.modules.auth.dtos.LoginRequest;
import com.helpdesk.backend.modules.auth.dtos.RefreshRequest;
import com.helpdesk.backend.modules.auth.dtos.RegisterRequest;
import com.helpdesk.backend.modules.auth.dtos.UserInfo;
import com.helpdesk.backend.modules.user.domain.Role;
import com.helpdesk.backend.modules.user.domain.User;
import com.helpdesk.backend.modules.user.domain.UserRepository;
import com.helpdesk.backend.shared.exception.BusinessException;
import com.helpdesk.backend.shared.security.JwtProvider;
import com.helpdesk.backend.shared.security.LoginRateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final LoginRateLimiter rateLimiter;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email already in use", HttpStatus.CONFLICT);
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.CLIENT)
                .build();

        user = userRepository.save(user);
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String key = request.email().toLowerCase();

        if (rateLimiter.isBlocked(key)) {
            throw new BusinessException("Too many login attempts. Try again in 5 minutes.",
                    HttpStatus.TOO_MANY_REQUESTS);
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    rateLimiter.recordFailure(key);
                    return new BusinessException("Invalid credentials", HttpStatus.UNAUTHORIZED);
                });

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            rateLimiter.recordFailure(key);
            throw new BusinessException("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }

        if (!user.getIsActive()) {
            throw new BusinessException("Account disabled", HttpStatus.FORBIDDEN);
        }

        rateLimiter.recordSuccess(key);
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        if (!jwtProvider.isValid(request.refreshToken())) {
            throw new BusinessException("Invalid refresh token", HttpStatus.UNAUTHORIZED);
        }

        String tokenHash = hashToken(request.refreshToken());
        RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException("Refresh token not found", HttpStatus.UNAUTHORIZED));

        if (!stored.isValid()) {
            throw new BusinessException("Refresh token expired or revoked", HttpStatus.UNAUTHORIZED);
        }

        // Revogar o token usado (rotation)
        stored.revoke();
        refreshTokenRepository.save(stored);

        // Gerar novos tokens
        User user = stored.getUser();
        return buildAuthResponse(user);
    }

    @Transactional
    public void logout(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    @Transactional(readOnly = true)
    public UserInfo me(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));

        return new UserInfo(
                user.getId().toString(),
                user.getName(),
                user.getEmail(),
                user.getRole().name());
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId(), user.getEmail(), user.getRole().name());

        // Salvar hash do refresh token no banco
        RefreshToken entity = RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(refreshToken))
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        refreshTokenRepository.save(entity);

        return new AuthResponse(
                accessToken,
                refreshToken,
                new UserInfo(user.getId().toString(), user.getName(), user.getEmail(), user.getRole().name()));
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}