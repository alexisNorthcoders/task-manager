package com.projects.taskmanager.controller;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import com.projects.taskmanager.model.Task;
import com.projects.taskmanager.model.TaskTemplate;
import com.projects.taskmanager.service.TaskTemplateService;
import com.projects.taskmanager.graphql.input.CreateTaskTemplateInput;
import com.projects.taskmanager.graphql.input.UpdateTaskTemplateInput;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import com.projects.taskmanager.observability.MetricsService;
import io.micrometer.core.instrument.Timer;

@Controller
@Validated
public class TaskTemplateController {
    
    private final TaskTemplateService taskTemplateService;
    private final MetricsService metricsService;

    public TaskTemplateController(TaskTemplateService taskTemplateService, MetricsService metricsService) {
        this.taskTemplateService = taskTemplateService;
        this.metricsService = metricsService;
    }

    @QueryMapping
    public List<TaskTemplate> taskTemplates() {
        Timer.Sample sample = metricsService.startGraphQLQueryTimer();
        try {
            return taskTemplateService.getAllTaskTemplates();
        } finally {
            metricsService.stopGraphQLQueryTimer(sample);
        }
    }

    @QueryMapping
    public TaskTemplate taskTemplate(@Argument Long id) {
        return taskTemplateService.getTaskTemplateById(id).orElse(null);
    }

    @MutationMapping
    @PreAuthorize("hasRole('USER')")
    public TaskTemplate createTaskTemplate(@Argument("input") @Valid CreateTaskTemplateInput input) {
        Timer.Sample sample = metricsService.startGraphQLMutationTimer();
        try {
            TaskTemplate template = new TaskTemplate();
            template.setName(input.getName());
            template.setTitle(input.getTitle());
            template.setDescription(input.getDescription());
            template.setEstimationHours(input.getEstimationHours());
            
            return taskTemplateService.createTaskTemplate(template);
        } finally {
            metricsService.stopGraphQLMutationTimer(sample);
        }
    }

    @MutationMapping
    @PreAuthorize("hasRole('USER')")
    public TaskTemplate updateTaskTemplate(@Argument Long id, @Argument("input") @Valid UpdateTaskTemplateInput input) {
        Timer.Sample sample = metricsService.startGraphQLMutationTimer();
        try {
            return taskTemplateService.updateTaskTemplate(
                id,
                input.getName(),
                input.getTitle(),
                input.getDescription(),
                input.getEstimationHours()
            );
        } finally {
            metricsService.stopGraphQLMutationTimer(sample);
        }
    }

    @MutationMapping
    @PreAuthorize("hasRole('USER')")
    public Boolean deleteTaskTemplate(@Argument Long id) {
        Timer.Sample sample = metricsService.startGraphQLMutationTimer();
        try {
            return taskTemplateService.deleteTaskTemplate(id);
        } finally {
            metricsService.stopGraphQLMutationTimer(sample);
        }
    }

    @MutationMapping
    @PreAuthorize("hasRole('USER')")
    public Task createTaskFromTemplate(@Argument Long templateId, @Argument List<Long> assignedUserIds) {
        Timer.Sample sample = metricsService.startGraphQLMutationTimer();
        try {
            return taskTemplateService.createTaskFromTemplate(templateId, assignedUserIds);
        } finally {
            metricsService.stopGraphQLMutationTimer(sample);
        }
    }
}