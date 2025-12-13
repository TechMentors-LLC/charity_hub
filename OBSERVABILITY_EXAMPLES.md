# Observability Examples and Quick Start

This guide provides practical examples for using the observability features in Charity Hub.

## Quick Start

### 1. Start the Application

```bash
./gradlew bootRun
```

### 2. Access Actuator Endpoints

Once the application is running, you can access the following endpoints:

```bash
# Health check
curl http://localhost:8080/actuator/health | jq

# All available metrics
curl http://localhost:8080/actuator/metrics | jq

# Prometheus format (for scraping)
curl http://localhost:8080/actuator/prometheus
```

## Viewing Custom Metrics

### Check Authentication Metrics

```bash
# Total authentication attempts
curl http://localhost:8080/actuator/metrics/charity_hub.authentication.attempts | jq

# Successful authentications
curl http://localhost:8080/actuator/metrics/charity_hub.authentication.successes | jq

# Failed authentications
curl http://localhost:8080/actuator/metrics/charity_hub.authentication.failures | jq

# Authentication duration (timing)
curl http://localhost:8080/actuator/metrics/charity_hub.authentication.duration | jq
```

### Check Business Metrics

```bash
# Cases created
curl http://localhost:8080/actuator/metrics/charity_hub.cases.created | jq

# Contributions made
curl http://localhost:8080/actuator/metrics/charity_hub.contributions.made | jq

# Notifications sent
curl http://localhost:8080/actuator/metrics/charity_hub.notifications.sent | jq
```

## Health Checks

### Overall Health

```bash
# All health indicators
curl http://localhost:8080/actuator/health | jq

# Expected response:
{
  "status": "UP",
  "components": {
    "diskSpace": { "status": "UP" },
    "firebase": { "status": "UP" },
    "mongo": { "status": "UP" },
    "ping": { "status": "UP" }
  }
}
```

### Kubernetes Probes

```bash
# Liveness probe (is app alive?)
curl http://localhost:8080/actuator/health/liveness

# Readiness probe (is app ready for traffic?)
curl http://localhost:8080/actuator/health/readiness
```

## Adding Metrics to Your Code

### Example 1: Simple Counter Metric

```java
@RestController
public class MyController {
    private final BusinessMetrics businessMetrics;

    public MyController(BusinessMetrics businessMetrics) {
        this.businessMetrics = businessMetrics;
    }

    @PostMapping("/cases")
    public ResponseEntity<CaseResponse> createCase(@RequestBody CreateCaseRequest request) {
        // Your business logic here
        Case newCase = caseService.create(request);
        
        // Record the metric
        businessMetrics.recordCaseCreation();
        
        return ResponseEntity.ok(new CaseResponse(newCase));
    }
}
```

### Example 2: Using @Observed for Repositories (Recommended)

Use `@Observed` for database operations to get full trace visibility:

```java
@Repository
public class ContributionRepository {
    
    @Observed(name = "charity_hub.repo.contributions.save", 
              contextualName = "contribution-repo-save")
    public void save(Contribution contribution) {
        // Database operation - will appear in trace waterfalls
    }
}
```

**Why @Observed for Repositories?**
- When debugging "database is slow", you need to know WHICH request caused it
- Trace context links the slow query back to the originating HTTP request
- Essential for debugging cascading latency issues

### Example 3: Using @Observed for Controllers/Handlers

```java
@Service
public class PaymentService {
    
    @Observed(name = "payment.process",
              contextualName = "process-payment",
              lowCardinalityKeyValues = {"service", "payment"})
    public Payment processPayment(PaymentRequest request) {
        // Your payment processing logic
        return payment;
    }
}
```

### Example 4: Combining @Observed with Custom Counters

Use `@Observed` for timing/tracing and custom counters for business metrics:

```java
@RestController
public class NotificationController {
    private final BusinessMetrics businessMetrics;
    private final NotificationService notificationService;

    @PostMapping("/notifications/send")
    // Use @Observed for controllers - it includes timing AND creates tracing spans
    // Do NOT use @Timed together with @Observed (redundant)
    @Observed(name = "notification.send", contextualName = "send-notification")
    public ResponseEntity<NotificationResponse> sendNotification(
            @RequestBody NotificationRequest request) {
        try {
            notificationService.send(request);
            businessMetrics.recordNotificationSent();
            return ResponseEntity.ok(new NotificationResponse("sent"));
        } catch (Exception e) {
            log.error("Failed to send notification", e);
            throw e;
        }
    }
}
```

## Custom Health Indicator Example

```java
package com.charity_hub.shared.observability.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class CustomServiceHealthIndicator implements HealthIndicator {

    private final CustomService customService;

    public CustomServiceHealthIndicator(CustomService customService) {
        this.customService = customService;
    }

    @Override
    public Health health() {
        try {
            // Check if service is available
            if (customService.isAvailable()) {
                return Health.up()
                    .withDetail("status", "Service is available")
                    .withDetail("lastCheck", System.currentTimeMillis())
                    .build();
            } else {
                return Health.down()
                    .withDetail("reason", "Service is unavailable")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .withException(e)
                .build();
        }
    }
}
```

## Testing Locally

### 1. Trigger Authentication

```bash
# Make an authentication request
curl -X POST http://localhost:8080/v1/accounts/authenticate \
  -H "Content-Type: application/json" \
  -d '{
    "firebaseToken": "test-token",
    "deviceId": "test-device-123",
    "platform": "ANDROID"
  }'
```

### 2. Check the Metrics

```bash
# View authentication attempts counter
curl http://localhost:8080/actuator/metrics/charity_hub.authentication.attempts | jq

# Should show increment in the count value
```

### 3. View Logs with Trace IDs

The logs will include trace correlation:

```
2024-12-08 INFO [charity_hub,67543a1b2c3d4e5f6789,1234567890abcdef] Processing authentication request
```

## Integration with Monitoring Tools

### Prometheus Configuration

Create a `prometheus.yml` file:

```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'charity_hub'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
```

Run Prometheus:

```bash
docker run -d -p 9090:9090 \
  -v $(pwd)/prometheus.yml:/etc/prometheus/prometheus.yml \
  prom/prometheus
```

Access Prometheus UI at: http://localhost:9090

### Grafana Dashboard

1. Add Prometheus as a data source in Grafana
2. Create a new dashboard
3. Add panels with PromQL queries:

```promql
# Authentication rate
rate(charity_hub_authentication_attempts_total[5m])

# Success rate
rate(charity_hub_authentication_successes_total[5m]) / 
rate(charity_hub_authentication_attempts_total[5m])

# Case creation rate
rate(charity_hub_cases_created_total[1h])

# HTTP request duration
histogram_quantile(0.95, 
  rate(http_server_requests_seconds_bucket[5m]))
```

### Jaeger for Distributed Tracing

1. Start Jaeger:

```bash
docker run -d --name jaeger \
  -p 4318:4318 \
  -p 16686:16686 \
  jaegertracing/all-in-one:latest
```

2. Configure the application to send traces:

```properties
# In application.properties
management.otlp.tracing.endpoint=http://localhost:4318/v1/traces
```

3. Access Jaeger UI at: http://localhost:16686

## Troubleshooting

### Metrics Not Showing Up

1. Verify the application is running: `curl http://localhost:8080/actuator/health`
2. Check if endpoints are exposed: `curl http://localhost:8080/actuator | jq`
3. Ensure the metric name is correct: `curl http://localhost:8080/actuator/metrics | jq '.names'`

### Health Check Failing

```bash
# Get detailed health information
curl http://localhost:8080/actuator/health | jq

# Check specific health indicator
curl http://localhost:8080/actuator/health/mongo | jq
curl http://localhost:8080/actuator/health/firebase | jq
```

### No Traces Appearing

1. Check tracing is enabled:
   ```bash
   curl http://localhost:8080/actuator/env | jq '.propertySources[] | select(.name == "applicationConfig: [classpath:/application.properties]") | .properties | select(. != null) | with_entries(select(.key | startswith("management.tracing")))'
   ```

2. Verify OTLP endpoint is configured and accessible
3. Check application logs for export errors

## Best Practices

1. **Use Appropriate Granularity**: Don't over-instrument. Focus on key business operations.

2. **Tag Metrics Properly**: Use tags to add dimensions to metrics:
   ```java
   Counter.builder("charity_hub.cases.created")
       .tag("type", caseType)
       .tag("status", status)
       .register(registry);
   ```

3. **Keep Metric Names Consistent**: Use the `charity_hub.*` prefix for all custom metrics.

4. **Use @Observed for Controllers, Handlers, AND Repositories**:
   ```java
   // Controllers/Handlers - use @Observed (includes timing + tracing)
   @Observed(name = "handler.create_case", contextualName = "create-case-handler")
   public void handle(CreateCase command) { ... }
   
   // Repositories - use @Observed (essential for tracing slow queries)
   @Observed(name = "charity_hub.repo.case.save", contextualName = "case-repo-save")
   public void save(Case case_) { ... }
   
   // Only use @Timed for internal utilities where tracing overhead matters
   @Timed(value = "charity_hub.util.parse")
   private void parseInput() { ... }
   ```

5. **Never use @Timed and @Observed together**: They are redundant. `@Observed` already includes timing metrics.

6. **Monitor Error Rates**: Track both successes and failures:
   ```java
   try {
       // operation
       businessMetrics.recordSuccess();
   } catch (Exception e) {
       businessMetrics.recordFailure();
       throw e;
   }
   ```

7. **Set Appropriate Sampling**: In production, consider reducing trace sampling:
   ```properties
   management.tracing.sampling.probability=0.1
   ```

## Advanced Examples

### Custom Metric with Tags

```java
@Component
public class AdvancedMetrics {
    private final MeterRegistry registry;

    public AdvancedMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordCaseCreation(String caseType, String status) {
        Counter.builder("charity_hub.cases.created.detailed")
            .description("Cases created by type and status")
            .tag("type", caseType)
            .tag("status", status)
            .register(registry)
            .increment();
    }

    public void recordContributionAmount(double amount, String currency) {
        DistributionSummary.builder("charity_hub.contributions.amount")
            .description("Distribution of contribution amounts")
            .baseUnit(currency)
            .register(registry)
            .record(amount);
    }
}
```

### Timer with Custom Tags

```java
@Service
public class TimedService {
    private final MeterRegistry registry;

    public void processWithTiming(String operationType) {
        Timer.Sample sample = Timer.start(registry);
        try {
            // Your operation here
        } finally {
            sample.stop(Timer.builder("charity_hub.operation.duration")
                .tag("type", operationType)
                .register(registry));
        }
    }
}
```

## Summary

With these observability features, you can:
- ✅ Monitor application health in real-time
- ✅ Track business metrics and KPIs
- ✅ Trace requests across services
- ✅ Debug issues with correlated logs
- ✅ Set up alerts and dashboards
- ✅ Ensure production readiness

For more details, see [OBSERVABILITY.md](./OBSERVABILITY.md).
