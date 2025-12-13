package com.charity_hub.shared.infrastructure;

import com.github.benmanes.caffeine.cache.Cache;
import io.github.bucket4j.Bucket;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that enforces rate limiting on API requests.
 * Uses a token bucket algorithm with separate limits for:
 * - General API requests (configurable, default: 100 requests per minute)
 * - Authentication requests (stricter, default: 10 requests per minute)
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnBean(RateLimitConfig.class)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);
    private static final String AUTH_PATH = "/v1/accounts/authenticate";
    private static final String REFRESH_TOKEN_PATH = "/v1/accounts/refresh-token";

    private final Cache<String, Bucket> rateLimitCache;
    private final Cache<String, Bucket> authRateLimitCache;
    private final RateLimitConfig rateLimitConfig;
    private final Counter rateLimitExceededCounter;
    private final Counter authRateLimitExceededCounter;

    public RateLimitFilter(
            Cache<String, Bucket> rateLimitCache,
            Cache<String, Bucket> authRateLimitCache,
            RateLimitConfig rateLimitConfig,
            MeterRegistry meterRegistry) {
        this.rateLimitCache = rateLimitCache;
        this.authRateLimitCache = authRateLimitCache;
        this.rateLimitConfig = rateLimitConfig;
        this.rateLimitExceededCounter = Counter.builder("charity_hub.rate_limit.exceeded")
                .tag("type", "general")
                .description("Number of rate limit exceeded events")
                .register(meterRegistry);
        this.authRateLimitExceededCounter = Counter.builder("charity_hub.rate_limit.exceeded")
                .tag("type", "authentication")
                .description("Number of authentication rate limit exceeded events")
                .register(meterRegistry);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String clientIp = getClientIp(request);
        String path = request.getRequestURI();

        // Skip rate limiting for actuator endpoints
        if (path.startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Use stricter rate limiting for authentication endpoints
        boolean isAuthEndpoint = path.equals(AUTH_PATH) || path.equals(REFRESH_TOKEN_PATH);

        Bucket bucket;
        if (isAuthEndpoint) {
            bucket = authRateLimitCache.get(clientIp, k -> rateLimitConfig.newAuthBucket());
        } else {
            bucket = rateLimitCache.get(clientIp, k -> rateLimitConfig.newBucket());
        }

        if (bucket.tryConsume(1)) {
            // Request allowed, add rate limit headers
            long remainingTokens = bucket.getAvailableTokens();
            response.addHeader("X-RateLimit-Remaining", String.valueOf(remainingTokens));
            filterChain.doFilter(request, response);
        } else {
            // Rate limit exceeded
            if (isAuthEndpoint) {
                authRateLimitExceededCounter.increment();
                log.warn("Authentication rate limit exceeded for IP: {}", clientIp);
            } else {
                rateLimitExceededCounter.increment();
                log.warn("Rate limit exceeded for IP: {} on path: {}", clientIp, path);
            }

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"description\":\"Rate limit exceeded. Please try again later.\"}");
        }
    }

    /**
     * Extracts the client IP address, considering X-Forwarded-For header for
     * proxied requests.
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Take the first IP in the chain (original client)
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
