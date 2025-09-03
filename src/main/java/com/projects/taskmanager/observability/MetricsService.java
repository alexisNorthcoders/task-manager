package com.projects.taskmanager.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Service for tracking custom application metrics
 */
@Service
public class MetricsService {

    private final MeterRegistry meterRegistry;
    
    // Counters for tracking operations
    private final Counter taskCreatedCounter;
    private final Counter taskUpdatedCounter;
    private final Counter taskDeletedCounter;
    private final Counter userCreatedCounter;
    private final Counter userUpdatedCounter;
    private final Counter userDeletedCounter;
    private final Counter authenticationCounter;
    private final Counter authenticationFailureCounter;
    
    // Timers for tracking latency
    private final Timer graphqlQueryTimer;
    private final Timer graphqlMutationTimer;
    private final Timer databaseQueryTimer;
    private final Timer authenticationTimer;

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Initialize counters
        this.taskCreatedCounter = Counter.builder("task.created")
                .description("Number of tasks created")
                .register(meterRegistry);
                
        this.taskUpdatedCounter = Counter.builder("task.updated")
                .description("Number of tasks updated")
                .register(meterRegistry);
                
        this.taskDeletedCounter = Counter.builder("task.deleted")
                .description("Number of tasks deleted")
                .register(meterRegistry);
                
        this.userCreatedCounter = Counter.builder("user.created")
                .description("Number of users created")
                .register(meterRegistry);
                
        this.userUpdatedCounter = Counter.builder("user.updated")
                .description("Number of users updated")
                .register(meterRegistry);
                
        this.userDeletedCounter = Counter.builder("user.deleted")
                .description("Number of users deleted")
                .register(meterRegistry);
                
        this.authenticationCounter = Counter.builder("authentication.success")
                .description("Number of successful authentications")
                .register(meterRegistry);
                
        this.authenticationFailureCounter = Counter.builder("authentication.failure")
                .description("Number of failed authentications")
                .register(meterRegistry);
        
        // Initialize timers
        this.graphqlQueryTimer = Timer.builder("graphql.query.duration")
                .description("GraphQL query execution time")
                .register(meterRegistry);
                
        this.graphqlMutationTimer = Timer.builder("graphql.mutation.duration")
                .description("GraphQL mutation execution time")
                .register(meterRegistry);
                
        this.databaseQueryTimer = Timer.builder("database.query.duration")
                .description("Database query execution time")
                .register(meterRegistry);
                
        this.authenticationTimer = Timer.builder("authentication.duration")
                .description("Authentication process duration")
                .register(meterRegistry);
    }

    // Counter methods
    public void incrementTaskCreated() {
        taskCreatedCounter.increment();
    }

    public void incrementTaskUpdated() {
        taskUpdatedCounter.increment();
    }

    public void incrementTaskDeleted() {
        taskDeletedCounter.increment();
    }

    public void incrementUserCreated() {
        userCreatedCounter.increment();
    }

    public void incrementUserUpdated() {
        userUpdatedCounter.increment();
    }

    public void incrementUserDeleted() {
        userDeletedCounter.increment();
    }

    public void incrementAuthenticationSuccess() {
        authenticationCounter.increment();
    }

    public void incrementAuthenticationFailure() {
        authenticationFailureCounter.increment();
    }

    // Timer methods
    public Timer.Sample startGraphQLQueryTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopGraphQLQueryTimer(Timer.Sample sample) {
        sample.stop(graphqlQueryTimer);
    }

    public Timer.Sample startGraphQLMutationTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopGraphQLMutationTimer(Timer.Sample sample) {
        sample.stop(graphqlMutationTimer);
    }

    public Timer.Sample startDatabaseQueryTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopDatabaseQueryTimer(Timer.Sample sample) {
        sample.stop(databaseQueryTimer);
    }

    public Timer.Sample startAuthenticationTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopAuthenticationTimer(Timer.Sample sample) {
        sample.stop(authenticationTimer);
    }

    // Convenience methods for recording durations
    public void recordGraphQLQueryDuration(Duration duration) {
        graphqlQueryTimer.record(duration);
    }

    public void recordGraphQLMutationDuration(Duration duration) {
        graphqlMutationTimer.record(duration);
    }

    public void recordDatabaseQueryDuration(Duration duration) {
        databaseQueryTimer.record(duration);
    }

    public void recordAuthenticationDuration(Duration duration) {
        authenticationTimer.record(duration);
    }

    // Gauge for active metrics
    public void recordActiveUsers(int count) {
        meterRegistry.gauge("users.active", count);
    }

    public void recordActiveTasks(int count) {
        meterRegistry.gauge("tasks.active", count);
    }
}
