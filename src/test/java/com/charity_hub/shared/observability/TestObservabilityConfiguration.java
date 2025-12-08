package com.charity_hub.shared.observability;

import com.charity_hub.shared.observability.metrics.BusinessMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Test configuration that provides observability beans for testing.
 * Use this configuration when testing controllers that use GlobalExceptionHandler.
 */
@TestConfiguration
public class TestObservabilityConfiguration {

    @Bean
    @Primary
    public MeterRegistry testMeterRegistry() {
        return new SimpleMeterRegistry();
    }

    @Bean
    @Primary
    public BusinessMetrics testBusinessMetrics(MeterRegistry registry) {
        return new BusinessMetrics(registry);
    }
}
