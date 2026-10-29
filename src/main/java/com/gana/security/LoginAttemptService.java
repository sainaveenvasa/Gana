package com.gana.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory brute-force protection for the login endpoint.
 *
 * Failures are tracked per (client IP, email) pair so an attacker cannot lock
 * a victim out from elsewhere, while repeated guessing against one account
 * from one address is throttled. State is per-instance and resets on restart.
 */
@Service
public class LoginAttemptService {

    private static final int MAX_TRACKED_KEYS = 10_000;

    private final int maxAttempts;
    private final Duration lockout;
    private final Map<String, Attempts> attempts = new ConcurrentHashMap<>();

    public LoginAttemptService(
            @Value("${app.security.login.max-attempts}") int maxAttempts,
            @Value("${app.security.login.lockout-minutes}") long lockoutMinutes
    ) {
        this.maxAttempts = maxAttempts;
        this.lockout = Duration.ofMinutes(lockoutMinutes);
    }

    public boolean isBlocked(String clientIp, String email) {
        Attempts entry = attempts.get(key(clientIp, email));
        if (entry == null) {
            return false;
        }
        if (entry.isExpired(lockout)) {
            attempts.remove(key(clientIp, email));
            return false;
        }
        return entry.count >= maxAttempts;
    }

    public void recordFailure(String clientIp, String email) {
        if (attempts.size() >= MAX_TRACKED_KEYS) {
            attempts.values().removeIf(a -> a.isExpired(lockout));
        }
        attempts.compute(key(clientIp, email), (k, existing) ->
                existing == null || existing.isExpired(lockout)
                        ? new Attempts(1, Instant.now())
                        : new Attempts(existing.count + 1, existing.firstFailure));
    }

    public void recordSuccess(String clientIp, String email) {
        attempts.remove(key(clientIp, email));
    }

    private static String key(String clientIp, String email) {
        return clientIp + "|" + email;
    }

    private record Attempts(int count, Instant firstFailure) {
        boolean isExpired(Duration window) {
            return firstFailure.plus(window).isBefore(Instant.now());
        }
    }
}
