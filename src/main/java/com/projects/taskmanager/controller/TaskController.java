package com.projects.taskmanager.controller;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.projects.taskmanager.model.Task;
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
    public Task task(@Argument Long id) {
        return taskService.getTaskById(id).orElse(null);
    }

    @MutationMapping
    public Task createTask(@Argument String title, @Argument String description, @Argument Boolean completed){
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setCompleted(completed != null && completed);
        return taskService.createTask(task);
    }

    @MutationMapping
    public Boolean deleteTask(@Argument Long id) {
        return taskService.deleteTask(id);
    }

    @MutationMapping
    public Task updateTask(@Argument Long id, @Argument String title, @Argument String description, @Argument Boolean completed) {
        return taskService.updateTask(id, title, description, completed);
    }
}
