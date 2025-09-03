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
import com.projects.taskmanager.service.TaskService;
import com.projects.taskmanager.service.UserService;
import com.projects.taskmanager.graphql.input.CreateTaskInput;
import com.projects.taskmanager.graphql.input.UpdateTaskInput;
import jakarta.validation.Valid;

@Controller
@Validated
public class TaskController {
    private final TaskService taskService;
    private final UserService userService;

    public TaskController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    @QueryMapping
    public List<Task> tasks() {
        return taskService.getAllTasks();
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
            @Argument Integer estimationHoursMin,
            @Argument Integer estimationHoursMax,
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
    public Task createTask(@Argument("input") @Valid CreateTaskInput input){
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

        // Handle user assignments if provided
        if (input.getAssignedUserIds() != null && !input.getAssignedUserIds().isEmpty()) {
            return userService.assignUsersToTask(createdTask.getId(), Set.copyOf(input.getAssignedUserIds()));
        }

        return createdTask;
    }

    @MutationMapping
    public Boolean deleteTask(@Argument Long id) {
        return taskService.deleteTask(id);
    }

    @MutationMapping
    public Task updateTask(@Argument Long id, @Argument("input") @Valid UpdateTaskInput input) {
        Task updatedTask = taskService.updateTask(
            id,
            input.getTitle(),
            input.getDescription(),
            input.getCompleted(),
            input.getDueDate(),
            input.getEstimationHours()
        );

        // Handle user assignments if provided
        if (input.getAssignedUserIds() != null) {
            return userService.assignUsersToTask(updatedTask.getId(), Set.copyOf(input.getAssignedUserIds()));
        }

        return updatedTask;
    }
}
