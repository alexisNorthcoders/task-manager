package com.projects.taskmanager.config;

import com.projects.taskmanager.observability.GraphQLRequestLoggingInterceptor;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for observability features including request logging
 */
@Configuration
public class ObservabilityConfig implements WebMvcConfigurer {

    private final GraphQLRequestLoggingInterceptor requestLoggingInterceptor;

    public ObservabilityConfig(GraphQLRequestLoggingInterceptor requestLoggingInterceptor) {
        this.requestLoggingInterceptor = requestLoggingInterceptor;
    }

    /**
     * Register HTTP request interceptor for GraphQL logging
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestLoggingInterceptor);
    }

    /**
     * Custom info contributor for Actuator info endpoint
     */
    @Bean
    public InfoContributor taskManagerInfoContributor() {
        return builder -> {
            Map<String, Object> details = new HashMap<>();
            details.put("name", "Task Manager API");
            details.put("description", "A GraphQL-based task management system with JWT authentication");
            details.put("version", "1.0.0");
            details.put("features", Map.of(
                "authentication", "JWT-based",
                "authorization", "Role-based (USER, ADMIN)",
                "database", "H2 (development), configurable",
                "api", "GraphQL with REST authentication endpoints",
                "observability", "Actuator, Prometheus metrics, structured logging"
            ));
            details.put("startup-time", Instant.now().toString());
            
            builder.withDetail("task-manager", details);
        };
    }
}
