package com.helpdesk.backend.shared.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimiter {
    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_SECONDS = 300; // 5 minutos

    private final Map<String, AttemptInfo> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String key) {
        AttemptInfo info = attempts.get(key);
        if (info == null)
            return false;

        if (info.windowStart.plusSeconds(WINDOW_SECONDS).isBefore(Instant.now())) {
            attempts.remove(key);
            return false;
        }

        return info.count >= MAX_ATTEMPTS;
    }

    public void recordFailure(String key) {
        attempts.compute(key, (k, info) -> {
            if (info == null || info.windowStart.plusSeconds(WINDOW_SECONDS).isBefore(Instant.now())) {
                return new AttemptInfo(Instant.now(), 1);
            }
            return new AttemptInfo(info.windowStart, info.count + 1);
        });
    }

    public void recordSuccess(String key) {
        attempts.remove(key);
    }

    private record AttemptInfo(Instant windowStart, int count) {
    }
}
