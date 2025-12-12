package com.charity_hub.shared.infrastructure;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Cache;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Configuration for rate limiting using Bucket4j and Caffeine cache.
 */
@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
@ConditionalOnProperty(name = "rate-limit.enabled", havingValue = "true", matchIfMissing = false)
public class RateLimitConfig {

    private final RateLimitProperties properties;

    public RateLimitConfig(RateLimitProperties properties) {
        this.properties = properties;
    }

    /**
     * Cache for storing rate limit buckets per IP address.
     */
    @Bean
    public Cache<String, Bucket> rateLimitCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(properties.windowSeconds() * 2L, TimeUnit.SECONDS)
                .maximumSize(100_000)
                .build();
    }

    /**
     * Cache for storing authentication rate limit buckets per IP address.
     */
    @Bean
    public Cache<String, Bucket> authRateLimitCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(properties.authWindowSeconds() * 2L, TimeUnit.SECONDS)
                .maximumSize(100_000)
                .build();
    }

    /**
     * Creates a new bucket for general API requests.
     */
    public Bucket newBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.simple(
                        properties.maxRequests(),
                        Duration.ofSeconds(properties.windowSeconds())))
                .build();
    }

    /**
     * Creates a new bucket for authentication requests (stricter limits).
     */
    public Bucket newAuthBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.simple(
                        properties.authMaxRequests(),
                        Duration.ofSeconds(properties.authWindowSeconds())))
                .build();
    }
}
