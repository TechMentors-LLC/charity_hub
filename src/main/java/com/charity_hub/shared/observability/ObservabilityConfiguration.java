package com.charity_hub.shared.observability;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import io.opentelemetry.api.OpenTelemetry; // Import 1
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender; // Import 2
import org.springframework.beans.factory.InitializingBean; // Import 3
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for observability features in Charity Hub.
 * Sets up aspects for @Timed and @Observed annotations.
 */
@Configuration
public class ObservabilityConfiguration {

    /**
     * Enable support for @Timed annotation on methods.
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    /**
     * Enable support for @Observed annotation on methods.
     */
    @Bean
    public ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
        return new ObservedAspect(observationRegistry);
    }

    /**
     * GLUE CODE: Connects the Spring-managed OpenTelemetry instance to the Logback Appender.
     * Without this, the Appender in logback-spring.xml doesn't know where to send logs.
     */
    @Bean
    public InitializingBean installOtelAppender(OpenTelemetry openTelemetry) {
        return () -> OpenTelemetryAppender.install(openTelemetry);
    }
}