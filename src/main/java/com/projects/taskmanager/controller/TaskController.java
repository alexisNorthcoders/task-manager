package com.projects.taskmanager.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import com.projects.taskmanager.model.Task;
import com.projects.taskmanager.model.TaskStatus;
import com.projects.taskmanager.model.User;
import com.projects.taskmanager.service.TaskService;
import com.projects.taskmanager.service.UserService;
import com.projects.taskmanager.graphql.input.CreateTaskInput;
import com.projects.taskmanager.graphql.input.UpdateTaskInput;
import com.projects.taskmanager.graphql.input.BulkUpdateTaskInput;
import com.projects.taskmanager.graphql.BulkOperationResult;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import com.projects.taskmanager.graphql.GraphQLUserContext;
import com.projects.taskmanager.observability.MetricsService;
import com.projects.taskmanager.service.TaskActivityService;
import com.projects.taskmanager.model.ActivityType;
import io.micrometer.core.instrument.Timer;

@Controller
@Validated
public class TaskController {
    private final TaskService taskService;
    private final UserService userService;
    private final GraphQLUserContext userContext;
    private final MetricsService metricsService;
    private final TaskActivityService taskActivityService;

    public TaskController(TaskService taskService, UserService userService, GraphQLUserContext userContext, MetricsService metricsService, TaskActivityService taskActivityService) {
        this.taskService = taskService;
        this.userService = userService;
        this.userContext = userContext;
        this.metricsService = metricsService;
        this.taskActivityService = taskActivityService;
    }

    @QueryMapping
    public List<Task> tasks() {
        Timer.Sample sample = metricsService.startGraphQLQueryTimer();
        try {
            List<Task> result = taskService.getAllTasks();
            metricsService.recordActiveTasks(result.size());
            return result;
        } finally {
            metricsService.stopGraphQLQueryTimer(sample);
        }
    }

    @QueryMapping
    public List<Task> tasksByStatus(@Argument TaskStatus status) {
        return taskService.getAllTasksByStatus(status);
    }

    @QueryMapping
    public Page<Task> tasksPaginated(
            @Argument Integer page,
            @Argument Integer size,
            @Argument Boolean completed,
            @Argument String titleContains,
            @Argument String sortBy,
            @Argument String sortDirection) {

        // Set defaults for optional parameters
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 10;
        String sortDir = sortDirection != null ? sortDirection : "ASC";

        return taskService.getTasksPaginated(pageNum, pageSize, completed, titleContains, sortBy, sortDir);
    }

    @QueryMapping
    public Page<Task> tasksAdvancedFiltered(
            @Argument Integer page,
            @Argument Integer size,
            @Argument Boolean completed,
            @Argument String titleContains,
            @Argument TaskStatus status,
            @Argument @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateFrom,
            @Argument @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateTo,
            @Argument Double estimationHoursMin,
            @Argument Double estimationHoursMax,
            @Argument String sortBy,
            @Argument String sortDirection) {

        // Set defaults for optional parameters
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 10;
        String sortDir = sortDirection != null ? sortDirection : "ASC";

        return taskService.getTasksAdvancedFiltered(
            pageNum, pageSize, completed, titleContains, status,
            dueDateFrom, dueDateTo, estimationHoursMin, estimationHoursMax,
            sortBy, sortDir);
    }

    @QueryMapping
    public Task task(@Argument Long id) {
        return taskService.getTaskById(id).orElse(null);
    }

    @MutationMapping
    @PreAuthorize("hasRole('USER')")
    public Task createTask(@Argument("input") @Valid CreateTaskInput input){
        Timer.Sample sample = metricsService.startGraphQLMutationTimer();
        try {
            Task task = new Task();
            task.setTitle(input.getTitle());
            task.setDescription(input.getDescription());
            task.setCompleted(input.getCompleted() != null && input.getCompleted());
            if (input.getDueDate() != null) {
                task.setDueDate(LocalDate.parse(input.getDueDate()));
            }
            if (input.getEstimationHours() != null) {
                task.setEstimationHours(input.getEstimationHours());
            }
            Task createdTask = taskService.createTask(task);

            // Log activity
            User currentUser = userContext.getCurrentUser();
            taskActivityService.logActivity(createdTask, currentUser, ActivityType.TASK_CREATED, 
                "Created task: " + createdTask.getTitle());

            // Handle user assignments if provided
            if (input.getAssignedUserIds() != null && !input.getAssignedUserIds().isEmpty()) {
                Task result = userService.assignUsersToTask(createdTask.getId(), Set.copyOf(input.getAssignedUserIds()));
                metricsService.incrementTaskCreated();
                return result;
            }

            metricsService.incrementTaskCreated();
            return createdTask;
        } finally {
            metricsService.stopGraphQLMutationTimer(sample);
        }
    }

    @MutationMapping
    @PreAuthorize("hasRole('USER')")
    public Boolean deleteTask(@Argument Long id) {
        Timer.Sample sample = metricsService.startGraphQLMutationTimer();
        try {
            // Get the task before deletion for activity logging
            Task taskToDelete = taskService.getTaskById(id).orElse(null);
            
            boolean result = taskService.deleteTask(id);
            if (result) {
                // Log activity
                User currentUser = userContext.getCurrentUser();
                if (taskToDelete != null) {
                    taskActivityService.logActivity(taskToDelete, currentUser, ActivityType.TASK_DELETED, 
                        "Deleted task: " + taskToDelete.getTitle());
                }
                metricsService.incrementTaskDeleted();
            }
            return result;
        } finally {
            metricsService.stopGraphQLMutationTimer(sample);
        }
    }

    @MutationMapping
    @PreAuthorize("hasRole('USER')")
    public Task updateTask(@Argument Long id, @Argument("input") @Valid UpdateTaskInput input) {
        Timer.Sample sample = metricsService.startGraphQLMutationTimer();
        try {
            // Get the task before update for activity logging
            Task originalTask = taskService.getTaskById(id).orElse(null);
            
            Task updatedTask = taskService.updateTask(
                id,
                input.getTitle(),
                input.getDescription(),
                input.getCompleted(),
                input.getStatus(),
                input.getDueDate(),
                input.getEstimationHours()
            );

            // Log activity for changes
            User currentUser = userContext.getCurrentUser();
            if (originalTask != null) {
                if (input.getTitle() != null && !input.getTitle().equals(originalTask.getTitle())) {
                    taskActivityService.logActivity(updatedTask, currentUser, ActivityType.TASK_UPDATED, 
                        "Updated title", originalTask.getTitle(), input.getTitle());
                }
                if (input.getStatus() != null && !input.getStatus().equals(originalTask.getStatus())) {
                    taskActivityService.logActivity(updatedTask, currentUser, ActivityType.TASK_STATUS_CHANGED, 
                        "Changed status", originalTask.getStatus().name(), input.getStatus().name());
                }
                if (input.getDueDate() != null && !input.getDueDate().equals(originalTask.getDueDate() != null ? originalTask.getDueDate().toString() : null)) {
                    taskActivityService.logActivity(updatedTask, currentUser, ActivityType.TASK_DUE_DATE_CHANGED, 
                        "Updated due date", originalTask.getDueDate() != null ? originalTask.getDueDate().toString() : "null", input.getDueDate());
                }
                if (input.getEstimationHours() != null && !input.getEstimationHours().equals(originalTask.getEstimationHours())) {
                    taskActivityService.logActivity(updatedTask, currentUser, ActivityType.TASK_ESTIMATION_CHANGED, 
                        "Updated estimation hours", originalTask.getEstimationHours() != null ? originalTask.getEstimationHours().toString() : "null", input.getEstimationHours().toString());
                }
            }

            // Handle user assignments if provided
            if (input.getAssignedUserIds() != null) {
                Task result = userService.assignUsersToTask(updatedTask.getId(), Set.copyOf(input.getAssignedUserIds()));
                metricsService.incrementTaskUpdated();
                return result;
            }

            metricsService.incrementTaskUpdated();
            return updatedTask;
        } finally {
            metricsService.stopGraphQLMutationTimer(sample);
        }
    }

    @MutationMapping
    @PreAuthorize("hasRole('USER')")
    public BulkOperationResult bulkUpdateTasks(@Argument List<Long> taskIds, @Argument("input") @Valid BulkUpdateTaskInput input) {
        Timer.Sample sample = metricsService.startGraphQLMutationTimer();
        try {
            BulkOperationResult result = taskService.bulkUpdateTasks(taskIds, input);
            
            // Handle user assignments if provided
            if (input.getAssignedUserIds() != null && !input.getAssignedUserIds().isEmpty() && result.isSuccess()) {
                BulkOperationResult assignResult = userService.bulkAssignUsers(taskIds, input.getAssignedUserIds());
                if (!assignResult.isSuccess()) {
                    result.getErrors().addAll(assignResult.getErrors());
                    result.setSuccess(false);
                }
            }
            
            return result;
        } finally {
            metricsService.stopGraphQLMutationTimer(sample);
        }
    }

    @MutationMapping
    @PreAuthorize("hasRole('USER')")
    public BulkOperationResult bulkDeleteTasks(@Argument List<Long> taskIds) {
        Timer.Sample sample = metricsService.startGraphQLMutationTimer();
        try {
            return taskService.bulkDeleteTasks(taskIds);
        } finally {
            metricsService.stopGraphQLMutationTimer(sample);
        }
    }

    @MutationMapping
    @PreAuthorize("hasRole('USER')")
    public BulkOperationResult bulkAssignUsers(@Argument List<Long> taskIds, @Argument List<Long> userIds) {
        Timer.Sample sample = metricsService.startGraphQLMutationTimer();
        try {
            return userService.bulkAssignUsers(taskIds, userIds);
        } finally {
            metricsService.stopGraphQLMutationTimer(sample);
        }
    }
}
