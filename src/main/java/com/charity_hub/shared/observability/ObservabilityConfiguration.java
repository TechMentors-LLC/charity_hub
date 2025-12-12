package com.charity_hub.shared.observability;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender; // Import 2
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.resources.Resource;
import org.springframework.beans.factory.InitializingBean; // Import 3
import org.springframework.beans.factory.annotation.Value;
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

    @SuppressWarnings("null")
    @Bean
    OtlpHttpLogRecordExporter otlpLogRecordExporter(
            @Value("${otel.exporter.otlp.logs.endpoint:http://localhost:4318/v1/logs}") String endpoint) {
        return OtlpHttpLogRecordExporter.builder()
                .setEndpoint(endpoint)
                .build();
    }

    @SuppressWarnings("null")
    @Bean
    SdkLoggerProvider sdkLoggerProvider(OtlpHttpLogRecordExporter logExporter,
            @Value("${spring.application.name:charity-hub}") String serviceName) {
        Resource resource = Resource.getDefault().merge(
                Resource.create(Attributes.of(AttributeKey.stringKey("service.name"), serviceName)));

        return SdkLoggerProvider.builder()
                .setResource(resource)
                .addLogRecordProcessor(BatchLogRecordProcessor.builder(logExporter).build())
                .build();
    }

    /**
     * GLUE CODE: Connects the Spring-managed OpenTelemetry instance to the Logback
     * Appender.
     * Without this, the Appender in logback-spring.xml doesn't know where to send
     * logs.
     */
    @SuppressWarnings("null")
    @Bean
    public InitializingBean installOtelAppender(SdkLoggerProvider sdkLoggerProvider) {
        // We create a dedicated OpenTelemetrySdk instance for logging to ensure the
        // LoggerProvider is correctly attached
        OpenTelemetrySdk loggingOpenTelemetry = OpenTelemetrySdk.builder()
                .setLoggerProvider(sdkLoggerProvider)
                .build();

        return () -> OpenTelemetryAppender.install(loggingOpenTelemetry);
    }
}