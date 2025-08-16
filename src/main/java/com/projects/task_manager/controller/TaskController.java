package com.projects.task_manager.controller;

import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.projects.task_manager.model.Task;
import com.projects.task_manager.service.TaskService;

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
    public Task createTask(@Argument String title, @Argument String description){
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setCompleted(false);
        return taskService.createTask(task);
    }

    @MutationMapping
    public Boolean deleteTask(@Argument Long id) {
        try {
            taskService.deleteTask(id);
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    @MutationMapping
    public Task updateTask(@Argument Long id, @Argument String title, @Argument String description, @Argument boolean completed) {
        Task updatedTask = new Task();
        updatedTask.setTitle(title);
        updatedTask.setDescription(description);
        updatedTask.setCompleted(completed);
        return taskService.updateTask(id, updatedTask);
    }
}
