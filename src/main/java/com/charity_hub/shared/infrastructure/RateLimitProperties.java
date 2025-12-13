package com.charity_hub.shared.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for rate limiting.
 */
@ConfigurationProperties(prefix = "rate-limit")
public record RateLimitProperties(
        /**
         * Whether rate limiting is enabled.
         */
        boolean enabled,

        /**
         * Maximum number of requests allowed per time window.
         */
        int maxRequests,

        /**
         * Time window in seconds.
         */
        int windowSeconds,

        /**
         * Maximum number of authentication attempts per time window.
         */
        int authMaxRequests,

        /**
         * Time window for authentication attempts in seconds.
         */
        int authWindowSeconds) {
    public RateLimitProperties {
        // Set defaults if not configured
        if (maxRequests <= 0)
            maxRequests = 100;
        if (windowSeconds <= 0)
            windowSeconds = 60;
        if (authMaxRequests <= 0)
            authMaxRequests = 10;
        if (authWindowSeconds <= 0)
            authWindowSeconds = 60;
    }
}
