/**
 * Observability infrastructure for Charity Hub.
 * 
 * <p>This package provides comprehensive observability features including:
 * <ul>
 *   <li>Health indicators for Firebase and MongoDB connectivity</li>
 *   <li>Custom business metrics for tracking key operations</li>
 *   <li>Distributed tracing support via OpenTelemetry</li>
 *   <li>Integration with Prometheus for metrics export</li>
 * </ul>
 * 
 * <h2>Usage</h2>
 * <p>Controllers and services can use the {@link com.charity_hub.shared.observability.metrics.BusinessMetrics}
 * component to track business operations. Actuator endpoints are exposed at {@code /actuator/*}.
 * 
 * <h2>Available Metrics</h2>
 * <ul>
 *   <li>{@code charity_hub.authentication.attempts} - Total authentication attempts</li>
 *   <li>{@code charity_hub.authentication.successes} - Successful authentications</li>
 *   <li>{@code charity_hub.authentication.failures} - Failed authentications</li>
 *   <li>{@code charity_hub.cases.created} - Cases created</li>
 *   <li>{@code charity_hub.contributions.made} - Contributions made</li>
 *   <li>{@code charity_hub.notifications.sent} - Notifications sent</li>
 * </ul>
 * 
 * @since 0.0.1
 */
package com.charity_hub.shared.observability;
