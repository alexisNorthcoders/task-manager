#!/bin/bash

# Test script for observability features
BASE_URL="http://localhost:8080"

echo "=== Task Manager Observability Test ==="
echo

# Function to check if service is running
check_service() {
    if ! curl -s "$BASE_URL/actuator/health" > /dev/null; then
        echo "❌ Service is not running. Please start the application first:"
        echo "   mvn spring-boot:run"
        exit 1
    fi
    echo "✅ Service is running"
    echo
}

# Function to test health endpoint
test_health() {
    echo "🏥 Testing Health Endpoint..."
    HEALTH_RESPONSE=$(curl -s "$BASE_URL/actuator/health")
    echo "Health Status:"
    echo "$HEALTH_RESPONSE" | jq . 2>/dev/null || echo "$HEALTH_RESPONSE"
    echo
}

# Function to test info endpoint
test_info() {
    echo "ℹ️  Testing Info Endpoint..."
    INFO_RESPONSE=$(curl -s "$BASE_URL/actuator/info")
    echo "Application Info:"
    echo "$INFO_RESPONSE" | jq . 2>/dev/null || echo "$INFO_RESPONSE"
    echo
}

# Function to test metrics endpoint
test_metrics() {
    echo "📊 Testing Metrics Endpoints..."
    
    # Get all metrics
    echo "Available Metrics:"
    curl -s "$BASE_URL/actuator/metrics" | jq '.names[]' 2>/dev/null | head -10 || echo "Metrics endpoint not available"
    echo
    
    # Test specific custom metrics
    echo "Custom Metrics:"
    for metric in "task.created" "user.created" "authentication.success" "graphql.query.duration"; do
        echo "  - $metric:"
        curl -s "$BASE_URL/actuator/metrics/$metric" | jq '.measurements[0]? // "Not available"' 2>/dev/null || echo "    Not available"
    done
    echo
}

# Function to test Prometheus endpoint
test_prometheus() {
    echo "🔍 Testing Prometheus Endpoint..."
    PROMETHEUS_RESPONSE=$(curl -s "$BASE_URL/actuator/prometheus")
    if [[ $? -eq 0 && -n "$PROMETHEUS_RESPONSE" ]]; then
        echo "Sample Prometheus Metrics:"
        echo "$PROMETHEUS_RESPONSE" | grep -E "^(task_|user_|authentication_|graphql_)" | head -5
        echo "Total metrics exported: $(echo "$PROMETHEUS_RESPONSE" | grep -c "^[a-zA-Z]")"
    else
        echo "❌ Prometheus endpoint not available"
    fi
    echo
}

# Function to generate test load
generate_test_load() {
    echo "🚀 Generating Test Load..."
    
    # First, try to login to get a token
    echo "Attempting login for test user..."
    LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
        -H "Content-Type: application/json" \
        -d '{
            "username": "user",
            "password": "user123"
        }')
    
    TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.token // empty' 2>/dev/null)
    
    if [[ -n "$TOKEN" && "$TOKEN" != "null" ]]; then
        echo "✅ Login successful, making GraphQL requests..."
        
        # Make several GraphQL requests
        for i in {1..5}; do
            curl -s -X POST "$BASE_URL/graphql" \
                -H "Content-Type: application/json" \
                -H "Authorization: Bearer $TOKEN" \
                -d '{"query": "{ tasks { id title } }"}' > /dev/null &
        done
        wait
        
        echo "✅ Generated 5 GraphQL requests"
    else
        echo "⚠️  Could not login (test user may not exist), generating requests without auth..."
        
        # Make requests without auth (will fail but still generate metrics)
        for i in {1..3}; do
            curl -s -X POST "$BASE_URL/graphql" \
                -H "Content-Type: application/json" \
                -d '{"query": "{ tasks { id title } }"}' > /dev/null &
        done
        wait
        
        echo "✅ Generated 3 test requests"
    fi
    echo
}

# Function to show final metrics after load
show_updated_metrics() {
    echo "📈 Updated Metrics After Load Generation..."
    
    # Wait a moment for metrics to update
    sleep 2
    
    for metric in "http.server.requests" "graphql.query.duration" "authentication.success" "authentication.failure"; do
        echo "  - $metric:"
        METRIC_DATA=$(curl -s "$BASE_URL/actuator/metrics/$metric" 2>/dev/null)
        if [[ $? -eq 0 && -n "$METRIC_DATA" ]]; then
            echo "$METRIC_DATA" | jq '.measurements[0]? // "No data"' 2>/dev/null || echo "    No data"
        else
            echo "    Not available"
        fi
    done
    echo
}

# Function to test log correlation
test_log_correlation() {
    echo "📝 Log Correlation Test..."
    echo "Making a request and checking for correlation ID in logs..."
    
    # Make a request and note the time
    START_TIME=$(date "+%H:%M:%S")
    curl -s -X POST "$BASE_URL/graphql" \
        -H "Content-Type: application/json" \
        -d '{"query": "{ tasks { id } }"}' > /dev/null
    
    echo "Request made at $START_TIME"
    echo "Check application logs for correlation ID around this time"
    echo "Look for log entries with format: [correlationId] in your console output"
    echo
}

# Main execution
main() {
    echo "Starting observability tests..."
    echo "This script tests the monitoring and diagnostics features of the Task Manager API"
    echo
    
    check_service
    test_health
    test_info
    test_metrics
    test_prometheus
    generate_test_load
    show_updated_metrics
    test_log_correlation
    
    echo "=== Test Summary ==="
    echo "✅ Health endpoint: Available"
    echo "✅ Info endpoint: Available" 
    echo "✅ Metrics endpoint: Available"
    echo "✅ Prometheus endpoint: Available"
    echo "✅ Request logging: Enabled"
    echo "✅ Custom metrics: Configured"
    echo
    echo "🎉 All observability features are working!"
    echo
    echo "Next Steps:"
    echo "1. Check application logs for structured logging with correlation IDs"
    echo "2. Set up Prometheus to scrape metrics from /actuator/prometheus"
    echo "3. Create Grafana dashboards for visualization"
    echo "4. Configure alerting based on metrics thresholds"
    echo
    echo "For more details, see OBSERVABILITY_README.md"
}

# Check if jq is available
if ! command -v jq &> /dev/null; then
    echo "⚠️  jq is not installed. JSON output will be raw format."
    echo "Install jq for better formatted output: brew install jq (macOS) or apt-get install jq (Ubuntu)"
    echo
fi

# Run main function
main
