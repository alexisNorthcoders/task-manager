package com.projects.taskmanager.observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.UUID;

/**
 * HTTP request interceptor for GraphQL logging and tracing
 * This provides observability for all GraphQL requests via HTTP
 */
@Component
public class GraphQLRequestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(GraphQLRequestLoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Only log GraphQL requests
        if (request.getRequestURI().contains("/graphql")) {
            String correlationId = UUID.randomUUID().toString();
            String method = request.getMethod();
            String uri = request.getRequestURI();
            
            // Add correlation ID to MDC for structured logging
            MDC.put("correlationId", correlationId);
            MDC.put("method", method);
            MDC.put("uri", uri);
            
            // Store start time and correlation ID in request attributes
            request.setAttribute("startTime", System.currentTimeMillis());
            request.setAttribute("correlationId", correlationId);
            
            logger.info("GraphQL request started - Method: [{}], URI: [{}], CorrelationId: [{}]", 
                       method, uri, correlationId);
        }
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // Only log GraphQL requests
        if (request.getRequestURI().contains("/graphql")) {
            Long startTime = (Long) request.getAttribute("startTime");
            String correlationId = (String) request.getAttribute("correlationId");
            
            if (startTime != null) {
                long duration = System.currentTimeMillis() - startTime;
                int status = response.getStatus();
                
                if (ex != null || status >= 400) {
                    logger.error("GraphQL request failed - CorrelationId: [{}], Duration: [{}ms], Status: [{}], Error: [{}]", 
                               correlationId, duration, status, ex != null ? ex.getMessage() : "HTTP Error");
                } else {
                    logger.info("GraphQL request completed - CorrelationId: [{}], Duration: [{}ms], Status: [{}]", 
                               correlationId, duration, status);
                }
            }
            
            // Clean up MDC
            MDC.remove("correlationId");
            MDC.remove("method");
            MDC.remove("uri");
        }
    }
}
