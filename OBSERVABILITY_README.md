# Observability Implementation - Monitoring & Diagnostics

This task manager includes comprehensive observability features for monitoring, diagnostics, and performance tracking.

## Features Implemented

### 1. Spring Boot Actuator
- **Health Checks**: `/actuator/health` - Application health status
- **Metrics**: `/actuator/metrics` - JVM and application metrics  
- **Info**: `/actuator/info` - Application information
- **Prometheus**: `/actuator/prometheus` - Prometheus-compatible metrics

### 2. Custom Metrics Tracking
- **Task Operations**: Create, update, delete counts
- **User Operations**: Registration, authentication tracking
- **Performance**: Request latency and duration metrics
- **GraphQL**: Query and mutation execution times

### 3. Structured Logging
- **Correlation IDs**: Every request gets a unique correlation ID
- **Request Logging**: All GraphQL/HTTP requests with timing
- **Contextual Logging**: MDC (Mapped Diagnostic Context) for structured logs
- **Debug Logging**: Detailed GraphQL and database operation logs

### 4. Request Tracing
- **HTTP Interceptor**: Tracks all GraphQL requests
- **Start/End Timing**: Request duration measurement
- **Error Tracking**: Failed request and error logging

## Available Endpoints

### Actuator Endpoints
```bash
# Health check
GET http://localhost:8080/actuator/health

# Application metrics
GET http://localhost:8080/actuator/metrics

# Prometheus metrics for external monitoring
GET http://localhost:8080/actuator/prometheus

# Application information
GET http://localhost:8080/actuator/info
```

### Health Check Response
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "H2",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 1000000000,
        "free": 500000000
      }
    }
  }
}
```

### Application Info Response
```json
{
  "task-manager": {
    "name": "Task Manager API",
    "description": "A GraphQL-based task management system with JWT authentication",
    "version": "1.0.0",
    "features": {
      "authentication": "JWT-based",
      "authorization": "Role-based (USER, ADMIN)",
      "database": "H2 (development), configurable",
      "api": "GraphQL with REST authentication endpoints",
      "observability": "Actuator, Prometheus metrics, structured logging"
    },
    "startup-time": "2025-01-01T12:00:00Z"
  }
}
```

## Custom Metrics Available

### Counters
- `task.created` - Number of tasks created
- `task.updated` - Number of tasks updated  
- `task.deleted` - Number of tasks deleted
- `user.created` - Number of users created
- `user.updated` - Number of users updated
- `user.deleted` - Number of users deleted
- `authentication.success` - Successful logins
- `authentication.failure` - Failed login attempts

### Timers
- `graphql.query.duration` - GraphQL query execution time
- `graphql.mutation.duration` - GraphQL mutation execution time
- `database.query.duration` - Database query execution time
- `authentication.duration` - Authentication process duration

### Gauges
- `users.active` - Number of active users
- `tasks.active` - Number of active tasks

## Viewing Metrics

### Get All Available Metrics
```bash
curl http://localhost:8080/actuator/metrics
```

### Get Specific Metric
```bash
# Task creation count
curl http://localhost:8080/actuator/metrics/task.created

# GraphQL query timing
curl http://localhost:8080/actuator/metrics/graphql.query.duration

# Authentication success rate
curl http://localhost:8080/actuator/metrics/authentication.success
```

### Prometheus Format
```bash
# Get all metrics in Prometheus format
curl http://localhost:8080/actuator/prometheus
```

## Log Structure

### Correlation ID Tracking
Every request gets a unique correlation ID for tracing across services:

```
2025-01-01 12:00:00 [http-nio-8080-exec-1] INFO [abc123-def456,] com.projects.taskmanager.observability.GraphQLRequestLoggingInterceptor - GraphQL request started - Method: [POST], URI: [/graphql], CorrelationId: [abc123-def456]
```

### Structured Log Format
```
%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [%X{traceId:-},%X{spanId:-}] %logger{36} - %msg%n
```

### Sample Log Output
```
2025-01-01 12:00:00 [http-nio-8080-exec-1] INFO  [abc123,] c.p.t.o.GraphQLRequestLoggingInterceptor - GraphQL request started - Method: [POST], URI: [/graphql], CorrelationId: [abc123]
2025-01-01 12:00:00 [http-nio-8080-exec-1] DEBUG [abc123,] c.p.t.service.TaskService - Creating new task: Test Task
2025-01-01 12:00:01 [http-nio-8080-exec-1] INFO  [abc123,] c.p.t.o.GraphQLRequestLoggingInterceptor - GraphQL request completed - CorrelationId: [abc123], Duration: [120ms], Status: [200]
```

## Configuration

### Logging Levels
```properties
# Enable detailed logging for our application
logging.level.com.projects.taskmanager=DEBUG

# Enable GraphQL debugging
logging.level.org.springframework.graphql=DEBUG
```

### Actuator Configuration
```properties
# Expose all actuator endpoints
management.endpoints.web.exposure.include=health,info,metrics,prometheus,httptrace,loggers

# Show detailed health information
management.endpoint.health.show-details=always

# Enable Prometheus metrics
management.metrics.export.prometheus.enabled=true
```

## Monitoring Integration

### Prometheus Setup
1. Configure Prometheus to scrape metrics:
```yaml
scrape_configs:
  - job_name: 'task-manager'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/actuator/prometheus'
```

### Grafana Dashboards
Create dashboards to visualize:
- Request rate and latency
- Task creation/completion trends
- Authentication success/failure rates
- Database query performance
- JVM metrics (heap, GC, threads)

### Alerting Rules
Set up alerts for:
- High error rates (>5%)
- Slow response times (>2s)
- Authentication failures spike
- Database connection issues
- High memory usage (>80%)

## Testing Observability

### Test Script
```bash
#!/bin/bash

echo "=== Testing Observability Features ==="

# Check health
echo "Health Check:"
curl -s http://localhost:8080/actuator/health | jq .

# Check application info  
echo "Application Info:"
curl -s http://localhost:8080/actuator/info | jq .

# Check specific metrics
echo "Task Creation Metric:"
curl -s http://localhost:8080/actuator/metrics/task.created | jq .

# Check Prometheus metrics
echo "Prometheus Metrics (sample):"
curl -s http://localhost:8080/actuator/prometheus | head -20
```

### Load Testing for Metrics
```bash
# Generate some load to see metrics in action
for i in {1..10}; do
  curl -X POST http://localhost:8080/graphql \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer YOUR_JWT_TOKEN" \
    -d '{"query": "{ tasks { id title } }"}' &
done
wait

# Check the metrics after load
curl -s http://localhost:8080/actuator/metrics/graphql.query.duration | jq .
```

## Best Practices

1. **Correlation IDs**: Always include correlation IDs in error reports
2. **Metric Naming**: Use consistent, descriptive metric names
3. **Log Levels**: Use appropriate log levels (INFO for business events, DEBUG for technical details)
4. **Performance**: Monitor response times and set up alerting
5. **Security**: Don't log sensitive information (passwords, tokens)
6. **Retention**: Configure log rotation and metric retention policies

## Troubleshooting

### Common Issues
1. **Missing Metrics**: Check if MetricsService is properly injected
2. **No Logs**: Verify logging configuration and levels
3. **Actuator 404**: Ensure actuator endpoints are exposed
4. **High Memory**: Monitor JVM metrics and tune garbage collection

### Debug Commands
```bash
# Check active logging levels
curl http://localhost:8080/actuator/loggers

# Check JVM metrics
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Check HTTP metrics
curl http://localhost:8080/actuator/metrics/http.server.requests
```
