package com.projects.taskmanager.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.projects.taskmanager.model.Task;
import com.projects.taskmanager.repository.TaskRepository;

/**
 * Service for managing tasks.
 */
@Service
public class TaskService {
    private final TaskRepository taskRepository;

    /**
     * Constructor for TaskService.
     * @param taskRepository the repository to use for task operations
     */
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Get all tasks from the database.
     * @return list of tasks
     */
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    /**
     * Get a task by its ID.
     * @param id
     * @return
     */
    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    /**
     * Create a new task.
     * @param task
     * @return
     */
    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    /**
     * Delete a task by its ID.
     * @param id
     */
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    /**
     * Update an existing task.
     * @param id
     * @param taskDetails
     * @return
     */
    public Task updateTask(Long id, Task taskDetails) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id " + id));
        task.setTitle(taskDetails.getTitle());
        task.setDescription(taskDetails.getDescription());
        task.setCompleted(taskDetails.isCompleted());
        return taskRepository.save(task);
    }
    
}
