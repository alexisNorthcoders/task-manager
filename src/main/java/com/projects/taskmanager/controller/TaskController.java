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
    public Task createTask(@Argument String title, @Argument String description, @Argument Boolean completed, @Argument String dueDate, @Argument Integer estimationHours){
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setCompleted(completed != null && completed);
        if (dueDate != null) {
            task.setDueDate(LocalDate.parse(dueDate));
        }
        if (estimationHours != null) {
            task.setEstimationHours(estimationHours);
        }
        return taskService.createTask(task);
    }

    @MutationMapping
    public Boolean deleteTask(@Argument Long id) {
        return taskService.deleteTask(id);
    }

    @MutationMapping
    public Task updateTask(@Argument Long id, @Argument String title, @Argument String description, @Argument Boolean completed, @Argument String dueDate, @Argument Integer estimationHours) {
        return taskService.updateTask(id, title, description, completed, dueDate, estimationHours);
    }
}
