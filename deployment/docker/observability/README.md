# Observability Stack for Charity Hub

This directory contains the complete observability infrastructure for Charity Hub, including metrics collection, visualization, distributed tracing, and alerting.

## 📁 Directory Structure

```
observability/
├── docker-compose.yml          # Main compose file for all services
├── README.md                   # This file
├── prometheus/
│   ├── prometheus.yml          # Prometheus configuration
│   └── alert-rules.yml         # Alert rules for Prometheus
├── grafana/
│   ├── provisioning/
│   │   ├── datasources/        # Grafana datasource configs
│   │   └── dashboards/         # Dashboard provisioning config
│   └── dashboards/             # Pre-built Grafana dashboards
└── alertmanager/
    └── alertmanager.yml        # Alertmanager configuration
```

## 🚀 Quick Start

### 1. Start the Observability Stack

```bash
cd deployment/docker/observability
docker-compose up -d
```

### 2. Start the Charity Hub Application

Make sure your application is running with the correct configuration:

```bash
# From project root
./gradlew bootRun
```

### 3. Access the Services

| Service | URL | Credentials |
|---------|-----|-------------|
| **Grafana** | http://localhost:3000 | admin / charity_hub_2024 |
| **Prometheus** | http://localhost:9090 | - |
| **Jaeger UI** | http://localhost:16686 | - |
| **AlertManager** | http://localhost:9093 | - |

## 📊 Available Dashboards

### Charity Hub Overview
Pre-configured dashboard showing:
- Application health status
- Request rate and response times
- Error rates
- Business metrics (auth, cases, contributions)
- JVM metrics (heap, threads)

## 🔧 Configuration

### Enable Distributed Tracing

Uncomment these lines in `application.properties`:

```properties
management.otlp.tracing.endpoint=http://localhost:4318/v1/traces
```

### Configure Alerts

1. Edit `alertmanager/alertmanager.yml` to add your notification channels:
   - Email (SMTP)
   - Slack webhooks
   - PagerDuty
   - Custom webhooks

2. Edit `prometheus/alert-rules.yml` to customize alert thresholds.

## 📈 Available Metrics

### Application Metrics (via Actuator)
```bash
curl http://localhost:8080/actuator/prometheus
```

### Custom Business Metrics

| Metric | Description |
|--------|-------------|
| `charity_hub_authentication_attempts_total` | Total authentication attempts |
| `charity_hub_authentication_successes_total` | Successful authentications |
| `charity_hub_authentication_failures_total` | Failed authentications |
| `charity_hub_cases_created_total` | Cases created |
| `charity_hub_contributions_made_total` | Contributions made |
| `charity_hub_notifications_sent_total` | Notifications sent |
| `charity_hub_exceptions_total` | Exceptions by type |

### Handler Timing Metrics

| Metric | Description |
|--------|-------------|
| `charity_hub_handler_authenticate_seconds` | Auth handler timing |
| `charity_hub_handler_create_case_seconds` | Create case timing |
| `charity_hub_handler_contribute_seconds` | Contribute handler timing |
| `charity_hub_handler_pay_contribution_seconds` | Pay contribution timing |

### Repository Timing Metrics

| Metric | Description |
|--------|-------------|
| `charity_hub_repo_case_*_seconds` | Case repository operations |
| `charity_hub_repo_account_*_seconds` | Account repository operations |
| `charity_hub_repo_contribution_*_seconds` | Contribution repository operations |

## 🔍 Using Distributed Tracing

### View Traces in Jaeger

1. Open http://localhost:16686
2. Select "charity-hub" from the Service dropdown
3. Click "Find Traces"

### Correlating Logs with Traces

Your application logs include `traceId` and `spanId`:
```
2024-12-08 10:15:30 INFO [charity_hub,abc123def456,789xyz] c.c.accounts.AuthController - Authentication attempt
```

Use these IDs to find the corresponding trace in Jaeger.

## 🛠️ Operations

### View Logs
```bash
docker-compose logs -f prometheus
docker-compose logs -f grafana
docker-compose logs -f jaeger
```

### Restart Services
```bash
docker-compose restart prometheus
docker-compose restart grafana
```

### Stop Everything
```bash
docker-compose down
```

### Stop and Remove Volumes
```bash
docker-compose down -v
```

## 🔔 Alerting

### Pre-configured Alerts

| Alert | Severity | Description |
|-------|----------|-------------|
| ApplicationDown | critical | App unreachable for >1 min |
| HighErrorRate | warning | Error rate >10% |
| HighAuthFailureRate | critical | Auth failures >30% |
| HighResponseTime | warning | P95 >2 seconds |
| LowHeapMemory | warning | Heap >90% |

### Test Alerts
```bash
# Check Prometheus alert status
curl http://localhost:9090/api/v1/alerts

# Check AlertManager status
curl http://localhost:9093/api/v1/alerts
```

## 📝 Useful PromQL Queries

### Request Rate
```promql
sum(rate(http_server_requests_seconds_count{job="charity-hub"}[5m]))
```

### Error Rate
```promql
sum(rate(http_server_requests_seconds_count{job="charity-hub",status=~"5.."}[5m])) 
/ sum(rate(http_server_requests_seconds_count{job="charity-hub"}[5m]))
```

### P95 Response Time
```promql
histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket{job="charity-hub"}[5m])) by (le))
```

### Authentication Success Rate
```promql
sum(rate(charity_hub_authentication_successes_total[5m])) 
/ sum(rate(charity_hub_authentication_attempts_total[5m]))
```

## 🐛 Troubleshooting

### Prometheus can't reach the application

1. Check if app is running: `curl http://localhost:8080/actuator/health`
2. Check Prometheus targets: http://localhost:9090/targets
3. On Mac, ensure `host.docker.internal` resolves correctly

### Grafana shows no data

1. Check Prometheus datasource: Grafana > Settings > Data Sources
2. Verify metrics exist: http://localhost:9090/graph
3. Check time range in dashboard

### Traces not appearing in Jaeger

1. Ensure tracing is enabled in `application.properties`
2. Check OTLP endpoint is correct
3. Verify Jaeger is receiving data: http://localhost:16686
