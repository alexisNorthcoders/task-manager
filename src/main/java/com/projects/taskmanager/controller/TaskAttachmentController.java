package com.projects.taskmanager.controller;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import org.springframework.security.access.prepost.PreAuthorize;

import com.projects.taskmanager.model.TaskAttachment;
import com.projects.taskmanager.model.User;
import com.projects.taskmanager.service.TaskAttachmentService;
import com.projects.taskmanager.graphql.GraphQLUserContext;
import com.projects.taskmanager.observability.MetricsService;
import io.micrometer.core.instrument.Timer;

@Controller
public class TaskAttachmentController {
    
    private final TaskAttachmentService taskAttachmentService;
    private final GraphQLUserContext userContext;
    private final MetricsService metricsService;
    
    public TaskAttachmentController(TaskAttachmentService taskAttachmentService,
                                  GraphQLUserContext userContext,
                                  MetricsService metricsService) {
        this.taskAttachmentService = taskAttachmentService;
        this.userContext = userContext;
        this.metricsService = metricsService;
    }
    
    @QueryMapping
    public List<TaskAttachment> taskAttachments(@Argument Long taskId) {
        Timer.Sample sample = metricsService.startGraphQLQueryTimer();
        try {
            return taskAttachmentService.getAttachmentsByTaskId(taskId);
        } finally {
            metricsService.stopGraphQLQueryTimer(sample);
        }
    }
    
    @QueryMapping
    public List<TaskAttachment> taskImages(@Argument Long taskId) {
        Timer.Sample sample = metricsService.startGraphQLQueryTimer();
        try {
            return taskAttachmentService.getImagesByTaskId(taskId);
        } finally {
            metricsService.stopGraphQLQueryTimer(sample);
        }
    }
    
    @QueryMapping
    public TaskAttachment taskAttachment(@Argument Long id) {
        Timer.Sample sample = metricsService.startGraphQLQueryTimer();
        try {
            return taskAttachmentService.getAttachmentById(id).orElse(null);
        } finally {
            metricsService.stopGraphQLQueryTimer(sample);
        }
    }
    
    @MutationMapping
    @PreAuthorize("hasRole('USER')")
    public Boolean deleteTaskAttachment(@Argument Long id) {
        Timer.Sample sample = metricsService.startGraphQLMutationTimer();
        try {
            User currentUser = userContext.getCurrentUser();
            return taskAttachmentService.deleteAttachment(id, currentUser);
        } finally {
            metricsService.stopGraphQLMutationTimer(sample);
        }
    }
    
    @SchemaMapping
    public Boolean isImage(TaskAttachment attachment) {
        return attachment.isImage();
    }
    
    @SchemaMapping
    public String fileSizeFormatted(TaskAttachment attachment) {
        return attachment.getFileSizeFormatted();
    }
}
