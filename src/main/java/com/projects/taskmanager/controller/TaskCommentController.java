package com.projects.taskmanager.controller;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import org.springframework.security.access.prepost.PreAuthorize;

import com.projects.taskmanager.model.TaskComment;
import com.projects.taskmanager.model.User;
import com.projects.taskmanager.service.TaskCommentService;
import com.projects.taskmanager.graphql.input.CreateTaskCommentInput;
import com.projects.taskmanager.graphql.input.UpdateTaskCommentInput;
import com.projects.taskmanager.graphql.GraphQLUserContext;
import com.projects.taskmanager.observability.MetricsService;
import io.micrometer.core.instrument.Timer;

@Controller
public class TaskCommentController {
    
    private final TaskCommentService taskCommentService;
    private final GraphQLUserContext userContext;
    private final MetricsService metricsService;
    
    public TaskCommentController(TaskCommentService taskCommentService, 
                               GraphQLUserContext userContext,
                               MetricsService metricsService) {
        this.taskCommentService = taskCommentService;
        this.userContext = userContext;
        this.metricsService = metricsService;
    }
    
    @QueryMapping
    public List<TaskComment> taskComments(@Argument Long taskId) {
        Timer.Sample sample = metricsService.startGraphQLQueryTimer();
        try {
            return taskCommentService.getCommentsByTaskId(taskId);
        } finally {
            metricsService.stopGraphQLQueryTimer(sample);
        }
    }
    
    @QueryMapping
    public TaskComment taskComment(@Argument Long id) {
        Timer.Sample sample = metricsService.startGraphQLQueryTimer();
        try {
            return taskCommentService.getCommentById(id).orElse(null);
        } finally {
            metricsService.stopGraphQLQueryTimer(sample);
        }
    }
    
    @MutationMapping
    @PreAuthorize("hasRole('USER')")
    public TaskComment createTaskComment(@Argument("input") CreateTaskCommentInput input) {
        Timer.Sample sample = metricsService.startGraphQLMutationTimer();
        try {
            User currentUser = userContext.getCurrentUser();
            return taskCommentService.createComment(input, currentUser);
        } finally {
            metricsService.stopGraphQLMutationTimer(sample);
        }
    }
    
    @MutationMapping
    @PreAuthorize("hasRole('USER')")
    public TaskComment updateTaskComment(@Argument Long id, @Argument("input") UpdateTaskCommentInput input) {
        Timer.Sample sample = metricsService.startGraphQLMutationTimer();
        try {
            User currentUser = userContext.getCurrentUser();
            return taskCommentService.updateComment(id, input, currentUser);
        } finally {
            metricsService.stopGraphQLMutationTimer(sample);
        }
    }
    
    @MutationMapping
    @PreAuthorize("hasRole('USER')")
    public Boolean deleteTaskComment(@Argument Long id) {
        Timer.Sample sample = metricsService.startGraphQLMutationTimer();
        try {
            User currentUser = userContext.getCurrentUser();
            return taskCommentService.deleteComment(id, currentUser);
        } finally {
            metricsService.stopGraphQLMutationTimer(sample);
        }
    }
    
    @SchemaMapping
    public List<TaskComment> replies(TaskComment comment) {
        return taskCommentService.getRepliesByCommentId(comment.getId());
    }
}
