package com.charity_hub.shared.observability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/**
 * Central class for business metrics tracking.
 * Provides custom metrics for key business operations in Charity Hub.
 */
@Component
public class BusinessMetrics {
    
    private final Counter authenticationAttempts;
    private final Counter authenticationSuccesses;
    private final Counter authenticationFailures;
    private final Counter caseCreations;
    private final Counter contributionsMade;
    private final Counter notificationsSent;
    private final Timer authenticationTimer;
    
    public BusinessMetrics(MeterRegistry registry) {
        // Authentication metrics
        this.authenticationAttempts = Counter.builder("charity_hub.authentication.attempts")
            .description("Total number of authentication attempts")
            .tag("type", "security")
            .register(registry);
            
        this.authenticationSuccesses = Counter.builder("charity_hub.authentication.successes")
            .description("Number of successful authentications")
            .tag("type", "security")
            .register(registry);
            
        this.authenticationFailures = Counter.builder("charity_hub.authentication.failures")
            .description("Number of failed authentications")
            .tag("type", "security")
            .register(registry);
            
        this.authenticationTimer = Timer.builder("charity_hub.authentication.duration")
            .description("Time taken for authentication operations")
            .tag("type", "performance")
            .register(registry);
        
        // Business operation metrics
        this.caseCreations = Counter.builder("charity_hub.cases.created")
            .description("Total number of charitable cases created")
            .tag("type", "business")
            .register(registry);
            
        this.contributionsMade = Counter.builder("charity_hub.contributions.made")
            .description("Total number of contributions made")
            .tag("type", "business")
            .register(registry);
            
        this.notificationsSent = Counter.builder("charity_hub.notifications.sent")
            .description("Total number of notifications sent")
            .tag("type", "business")
            .register(registry);
    }
    
    public void recordAuthenticationAttempt() {
        authenticationAttempts.increment();
    }
    
    public void recordAuthenticationSuccess() {
        authenticationSuccesses.increment();
    }
    
    public void recordAuthenticationFailure() {
        authenticationFailures.increment();
    }
    
    public Timer getAuthenticationTimer() {
        return authenticationTimer;
    }
    
    public void recordCaseCreation() {
        caseCreations.increment();
    }
    
    public void recordContribution() {
        contributionsMade.increment();
    }
    
    public void recordNotificationSent() {
        notificationsSent.increment();
    }
}
