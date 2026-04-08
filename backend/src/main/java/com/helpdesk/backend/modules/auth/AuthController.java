package com.helpdesk.backend.modules.auth;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.helpdesk.backend.modules.auth.dtos.AuthResponse;
import com.helpdesk.backend.modules.auth.dtos.LoginRequest;
import com.helpdesk.backend.modules.auth.dtos.RefreshRequest;
import com.helpdesk.backend.modules.auth.dtos.RegisterRequest;
import com.helpdesk.backend.modules.auth.dtos.UserInfo;
import com.helpdesk.backend.shared.dto.ApiResponse;
import com.helpdesk.backend.shared.security.CurrentUser;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(authService.register(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(request)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.refresh(request)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfo>> me(@CurrentUser UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(authService.me(userId)));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CurrentUser UUID userId) {
        authService.logout(userId);
        return ResponseEntity.noContent().build();
    }

}
