package com.charity_hub.testconfig;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Test security configuration that disables all security for integration tests.
 * This configuration takes precedence over the main SecurityConfig.
 * 
 * <p>
 * <strong>Security Note:</strong> CSRF protection is intentionally disabled
 * here
 * because this is a test-only configuration. This class is NOT used in
 * production.
 * The @TestConfiguration annotation ensures this bean is only loaded during
 * tests.
 * </p>
 */
@TestConfiguration
@SuppressWarnings("java:S4502") // CSRF is intentionally disabled for test security config
public class TestSecurityConfig {

    /**
     * Creates a permissive security filter chain for testing purposes.
     * CSRF is disabled because tests don't use browser sessions and
     * stateless API testing doesn't require CSRF protection.
     */
    @Bean(name = "testSecurityFilterChain")
    @Primary
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/**")
                // CSRF disabled intentionally for test environment - not used in production
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
