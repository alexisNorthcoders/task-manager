package com.projects.taskmanager.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.projects.taskmanager.model.Task;
import com.projects.taskmanager.model.TaskStatus;
import com.projects.taskmanager.service.TaskService;
import com.projects.taskmanager.graphql.input.CreateTaskInput;
import com.projects.taskmanager.graphql.input.UpdateTaskInput;

@Controller
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
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
    public Task task(@Argument Long id) {
        return taskService.getTaskById(id).orElse(null);
    }

    @MutationMapping
    public Task createTask(@Argument("input") CreateTaskInput input){
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
        return taskService.createTask(task);
    }

    @MutationMapping
    public Boolean deleteTask(@Argument Long id) {
        return taskService.deleteTask(id);
    }

    @MutationMapping
    public Task updateTask(@Argument Long id, @Argument("input") UpdateTaskInput input) {
        return taskService.updateTask(
            id,
            input.getTitle(),
            input.getDescription(),
            input.getCompleted(),
            input.getDueDate(),
            input.getEstimationHours()
        );
    }
}
