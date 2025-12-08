# Observability Guide for Charity Hub

This document provides a comprehensive guide to the observability features implemented in Charity Hub.

## Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Actuator Endpoints](#actuator-endpoints)
- [Metrics](#metrics)
- [Distributed Tracing](#distributed-tracing)
- [Health Checks](#health-checks)
- [Configuration](#configuration)
- [Usage Examples](#usage-examples)
- [Integration with External Systems](#integration-with-external-systems)

## Overview

Charity Hub implements comprehensive observability using Spring Boot Actuator, Micrometer, and OpenTelemetry. The system provides:

- **Metrics Collection**: Custom business metrics and system metrics via Prometheus
- **Distributed Tracing**: Request tracing across services using OpenTelemetry
- **Health Monitoring**: Custom health indicators for critical dependencies
- **Structured Logging**: ECS-formatted logs with trace correlation

## Key Features

### 1. Metrics Collection
- **Prometheus Metrics**: Exposed at `/actuator/prometheus`
- **Custom Business Metrics**: Track authentication, cases, contributions, and notifications
- **JVM Metrics**: Memory, threads, garbage collection
- **System Metrics**: CPU, disk, network

### 2. Distributed Tracing
- **OpenTelemetry**: OTLP exporter for trace data
- **Trace Propagation**: Automatic context propagation across modules
- **Correlation IDs**: Logs include traceId and spanId for correlation
- **100% Sampling**: All requests are traced (configurable)

### 3. Health Indicators
- **MongoDB**: Connection status and availability
- **Firebase**: Service initialization and connectivity
- **Liveness/Readiness**: Kubernetes-compatible health probes

## Actuator Endpoints

All actuator endpoints are accessible at `/actuator/*`. The following endpoints are enabled:

| Endpoint | Description | URL |
|----------|-------------|-----|
| Health | Application health status | `/actuator/health` |
| Info | Application information | `/actuator/info` |
| Metrics | Available metrics | `/actuator/metrics` |
| Prometheus | Prometheus-formatted metrics | `/actuator/prometheus` |
| Env | Environment properties | `/actuator/env` |
| Loggers | Log levels management | `/actuator/loggers` |
| Thread Dump | Current thread dump | `/actuator/threaddump` |
| Heap Dump | JVM heap dump | `/actuator/heapdump` |

### Accessing Endpoints

```bash
# Check application health
curl http://localhost:8080/actuator/health

# Get Prometheus metrics
curl http://localhost:8080/actuator/metrics

# View specific metric
curl http://localhost:8080/actuator/metrics/charity_hub.authentication.attempts
```

## Metrics

### Custom Business Metrics

The following custom metrics are tracked:

#### Authentication Metrics
- `charity_hub.authentication.attempts` - Total authentication attempts
- `charity_hub.authentication.successes` - Successful authentications
- `charity_hub.authentication.failures` - Failed authentications
- `charity_hub.authentication.duration` - Time taken for authentication
- `charity_hub.authentication.request` - HTTP request timing for auth endpoint

#### Business Operation Metrics
- `charity_hub.cases.created` - Number of charitable cases created
- `charity_hub.contributions.made` - Number of contributions made
- `charity_hub.notifications.sent` - Number of notifications sent

### System Metrics

Standard Spring Boot and JVM metrics are also available:
- `jvm.memory.*` - JVM memory usage
- `jvm.gc.*` - Garbage collection metrics
- `process.cpu.usage` - CPU usage
- `http.server.requests` - HTTP request metrics
- `system.cpu.usage` - System CPU usage

### Using Metrics in Code

Inject the `BusinessMetrics` component to track business operations:

```java
@RestController
public class MyController {
    private final BusinessMetrics businessMetrics;

    public MyController(BusinessMetrics businessMetrics) {
        this.businessMetrics = businessMetrics;
    }

    @PostMapping("/create-case")
    public ResponseEntity<CaseResponse> createCase(@RequestBody CreateCaseRequest request) {
        // Your business logic
        businessMetrics.recordCaseCreation();
        return ResponseEntity.ok(response);
    }
}
```

### Using Annotations

**For Controllers, Handlers, and Repositories (with tracing):** Use `@Observed` - it provides timing, tracing, and spans:

```java
@Observed(name = "operation.name", contextualName = "operation-context")
public void myOperation() {
    // Your code - timing AND tracing are automatically recorded
}
```

This is the preferred annotation for:
- **Controllers** - HTTP request handling
- **Handlers** - Business logic commands/queries
- **Repositories** - Database operations (essential for debugging slow queries in trace waterfalls)

**For simple timing only (internal utilities, tight loops):** Use `@Timed` only when you need simple timing without trace context overhead:

```java
@Timed(value = "operation.name", description = "Operation description")
public void internalUtility() {
    // Simple timing without tracing overhead
}
```

> **Important:** 
> - Do NOT use both `@Timed` and `@Observed` on the same method. `@Observed` already includes timing, so using both creates duplicate metrics.
> - Prefer `@Observed` for Repositories because trace context shows you WHICH request caused a slow DB query.

## Distributed Tracing

### OpenTelemetry Configuration

Traces are collected and can be exported to any OTLP-compatible backend (Jaeger, Tempo, etc.).

**To enable trace export**, configure the OTLP endpoint in `application.properties`:

```properties
management.otlp.tracing.endpoint=http://localhost:4318/v1/traces
```

### Trace Correlation in Logs

All logs include trace context for correlation:

```
2024-12-08 INFO [charity_hub,67543a1b2c3d4e5f6789,1234567890abcdef] Processing authentication request
```

Format: `[application-name, traceId, spanId]`

### Baggage Propagation

Custom baggage fields are configured:
- `userId` - User identifier
- `requestId` - Request identifier

These fields are propagated across service boundaries and included in traces.

## Health Checks

### Available Health Indicators

1. **MongoDB Health** (`/actuator/health`)
   - Checks MongoDB connectivity
   - Included automatically via Spring Boot

2. **Firebase Health** (`/actuator/health`)
   - Verifies Firebase app initialization
   - Custom implementation

3. **Liveness Probe** (`/actuator/health/liveness`)
   - Kubernetes liveness check
   - Returns UP if application is running

4. **Readiness Probe** (`/actuator/health/readiness`)
   - Kubernetes readiness check
   - Returns UP when application is ready to accept traffic

### Health Check Response Example

```json
{
  "status": "UP",
  "components": {
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 250685575168,
        "free": 100000000000,
        "threshold": 10485760,
        "path": "/home/runner/work/charity_hub/charity_hub",
        "exists": true
      }
    },
    "firebase": {
      "status": "UP",
      "details": {
        "appCount": 1,
        "status": "Firebase is initialized and ready"
      }
    },
    "mongo": {
      "status": "UP",
      "details": {
        "version": "7.0.0"
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

## Configuration

### Key Configuration Properties

Located in `src/main/resources/application.properties`:

```properties
# Actuator Endpoints
management.endpoints.web.exposure.include=health,info,metrics,prometheus,env,loggers,threaddump,heapdump
management.endpoint.health.show-details=when-authorized
management.endpoint.health.probes.enabled=true

# Health Checks
management.health.livenessstate.enabled=true
management.health.readinessstate.enabled=true
management.health.mongo.enabled=true

# Metrics
management.metrics.tags.application=${spring.application.name}
management.metrics.tags.environment=${ENVIRONMENT:local}
management.metrics.distribution.percentiles-histogram.http.server.requests=true

# Prometheus
management.prometheus.metrics.export.enabled=true

# Tracing
management.tracing.sampling.probability=1.0
management.tracing.enabled=true
management.tracing.baggage.enabled=true
management.tracing.baggage.correlation.enabled=true
management.tracing.baggage.correlation.fields=userId,requestId

# Logging with Trace Correlation
logging.pattern.level=%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]
logging.structured.format.console=ecs
```

### Environment-Specific Configuration

Use environment variables to customize observability settings:

```bash
# Set environment tag for metrics
export ENVIRONMENT=production

# Configure OTLP endpoint for traces
export MANAGEMENT_OTLP_TRACING_ENDPOINT=http://tempo:4318/v1/traces

# Configure MongoDB credentials
export MONGO_USERNAME=admin
export MONGO_PASSWORD=secure_password
```

## Usage Examples

### Monitoring Authentication

```bash
# Check authentication metrics
curl http://localhost:8080/actuator/metrics/charity_hub.authentication.attempts
curl http://localhost:8080/actuator/metrics/charity_hub.authentication.successes
curl http://localhost:8080/actuator/metrics/charity_hub.authentication.failures

# Check authentication timing
curl http://localhost:8080/actuator/metrics/charity_hub.authentication.duration
```

### Viewing All Available Metrics

```bash
# List all metrics
curl http://localhost:8080/actuator/metrics | jq '.names'

# Export for Prometheus
curl http://localhost:8080/actuator/prometheus
```

### Checking Application Health

```bash
# Overall health
curl http://localhost:8080/actuator/health

# Liveness probe
curl http://localhost:8080/actuator/health/liveness

# Readiness probe
curl http://localhost:8080/actuator/health/readiness
```

### Adjusting Log Levels

```bash
# Get current log level for a package
curl http://localhost:8080/actuator/loggers/com.charity_hub.accounts

# Change log level (POST request)
curl -X POST http://localhost:8080/actuator/loggers/com.charity_hub.accounts \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel":"DEBUG"}'
```

## Integration with External Systems

### Prometheus

Configure Prometheus to scrape metrics:

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'charity_hub'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
    static_configs:
      - targets: ['localhost:8080']
```

### Grafana Dashboards

Import Spring Boot dashboard:
- Dashboard ID: 11955 (JVM Micrometer)
- Dashboard ID: 12900 (Spring Boot 2.1 Statistics)

Or create custom dashboards using the charity_hub.* metrics.

### Jaeger / Tempo (Distributed Tracing)

Configure OTLP endpoint in application.properties:

```properties
management.otlp.tracing.endpoint=http://jaeger:4318/v1/traces
# or
management.otlp.tracing.endpoint=http://tempo:4318/v1/traces
```

### Kubernetes Deployment

Configure liveness and readiness probes:

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: charity-hub
spec:
  containers:
  - name: charity-hub
    image: charity-hub:latest
    livenessProbe:
      httpGet:
        path: /actuator/health/liveness
        port: 8080
      initialDelaySeconds: 30
      periodSeconds: 10
    readinessProbe:
      httpGet:
        path: /actuator/health/readiness
        port: 8080
      initialDelaySeconds: 20
      periodSeconds: 5
```

## Security Considerations

1. **Actuator Endpoint Security**: Some endpoints (heapdump, env) expose sensitive information. Secure them appropriately in production.

2. **Authentication**: Consider adding authentication to actuator endpoints:
   ```properties
   management.endpoints.web.exposure.include=health,metrics,prometheus
   management.endpoint.health.show-details=when-authorized
   ```

3. **Network Isolation**: Expose actuator endpoints only to monitoring infrastructure, not public internet.

4. **Trace Sampling**: In production, consider reducing sampling probability to control overhead:
   ```properties
   management.tracing.sampling.probability=0.1
   ```

## Troubleshooting

### Metrics Not Showing

1. Check if actuator dependency is present
2. Verify endpoints are exposed in configuration
3. Access `/actuator` to list available endpoints

### Traces Not Being Exported

1. Verify OTLP endpoint is configured and accessible
2. Check trace backend (Jaeger/Tempo) is running
3. Review application logs for export errors

### Health Check Failing

1. Check MongoDB connectivity
2. Verify Firebase credentials are configured
3. Review individual health indicator details

## Deployment Infrastructure

A complete Docker Compose stack for observability is available in the `deployment/docker/observability/` directory. This includes:

- **Prometheus** - Metrics collection and alerting
- **Grafana** - Pre-built dashboards and visualization
- **Jaeger** - Distributed tracing UI
- **AlertManager** - Alert routing and notifications

### Quick Start

```bash
# Start the observability stack
cd deployment/docker/observability
docker-compose up -d

# Access the services:
# - Grafana:      http://localhost:3000 (admin/charity_hub_2024)
# - Prometheus:   http://localhost:9090
# - Jaeger:       http://localhost:16686
# - AlertManager: http://localhost:9093
```

For detailed setup instructions, see the [Observability Stack README](deployment/docker/observability/README.md).

## Additional Resources

- [Spring Boot Actuator Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer Documentation](https://micrometer.io/docs)
- [OpenTelemetry Java Documentation](https://opentelemetry.io/docs/instrumentation/java/)
- [Prometheus Documentation](https://prometheus.io/docs)

## Contributing

When adding new observability features:

1. Add custom metrics to `BusinessMetrics` class
2. Use `@Timed` and `@Observed` annotations for new operations
3. Update this documentation with new metrics and endpoints
4. Add health indicators for new external dependencies
