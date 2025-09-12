package com.projects.taskmanager.controller;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.projects.taskmanager.model.TaskActivity;
import com.projects.taskmanager.service.TaskActivityService;
import com.projects.taskmanager.observability.MetricsService;
import io.micrometer.core.instrument.Timer;

@Controller
public class TaskActivityController {
    
    private final TaskActivityService taskActivityService;
    private final MetricsService metricsService;
    
    public TaskActivityController(TaskActivityService taskActivityService, MetricsService metricsService) {
        this.taskActivityService = taskActivityService;
        this.metricsService = metricsService;
    }
    
    @QueryMapping
    public List<TaskActivity> taskActivities(@Argument Long taskId) {
        Timer.Sample sample = metricsService.startGraphQLQueryTimer();
        try {
            return taskActivityService.getActivitiesByTaskId(taskId);
        } finally {
            metricsService.stopGraphQLQueryTimer(sample);
        }
    }
}
