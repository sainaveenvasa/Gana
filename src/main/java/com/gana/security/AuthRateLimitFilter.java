package com.gana.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-IP limit on the credential endpoints, which are public and run BCrypt.
 *
 * LoginAttemptService only throttles one (IP, email) pair, so without this a
 * single address could try a password against unlimited accounts or tie up
 * the CPU hashing. State is per-instance and resets on restart.
 */
@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_TRACKED_IPS = 100_000;

    private static final RequestMatcher RATE_LIMITED_REQUESTS = new OrRequestMatcher(
            PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/api/auth/login"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/api/auth/register")
    );

    private final int maxRequests;
    private final Duration window;
    private final Map<String, RequestWindow> windows = new ConcurrentHashMap<>();

    public AuthRateLimitFilter(
            @Value("${app.security.auth-rate-limit.max-requests}") int maxRequests,
            @Value("${app.security.auth-rate-limit.window-seconds}") long windowSeconds
    ) {
        this.maxRequests = maxRequests;
        this.window = Duration.ofSeconds(windowSeconds);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return !RATE_LIMITED_REQUESTS.matches(request);
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        // getRemoteAddr() rather than X-Forwarded-For, which clients can spoof.
        if (isAllowed(request.getRemoteAddr())) {
            filterChain.doFilter(request, response);
            return;
        }
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(window.toSeconds()));
        SecurityErrorWriter.write(response, HttpStatus.TOO_MANY_REQUESTS, "Too many requests. Try again later.");
    }

    private boolean isAllowed(String clientIp) {
        if (windows.size() >= MAX_TRACKED_IPS) {
            windows.values().removeIf(w -> w.isExpired(window));
            if (windows.size() >= MAX_TRACKED_IPS) {
                return false;
            }
        }
        RequestWindow current = windows.compute(clientIp, (ip, existing) ->
                existing == null || existing.isExpired(window)
                        ? new RequestWindow(1, Instant.now())
                        : new RequestWindow(existing.count + 1, existing.start));
        return current.count <= maxRequests;
    }

    private record RequestWindow(int count, Instant start) {
        boolean isExpired(Duration length) {
            return start.plus(length).isBefore(Instant.now());
        }
    }
}
