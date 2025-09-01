package com.projects.taskmanager.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.projects.taskmanager.config.TaskProperties;
import com.projects.taskmanager.model.Task;
import com.projects.taskmanager.model.TaskStatus;
import com.projects.taskmanager.repository.TaskRepository;
import com.projects.taskmanager.util.TextNormalizer;

/**
 * Service for managing tasks.
 */
@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskProperties taskProperties;
    private final TextNormalizer textNormalizer;

    /**
     * Constructor for TaskService.
     * @param taskRepository the repository to use for task operations
     */
    public TaskService(TaskRepository taskRepository, TaskProperties taskProperties, TextNormalizer textNormalizer) {
        this.taskRepository = taskRepository;
        this.taskProperties = taskProperties;
        this.textNormalizer = textNormalizer;
    }

    /**
     * Get all tasks from the database.
     * @return list of tasks
     */
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    /**
     * Get all tasks by status.
     * @param status
     * @return
     */
    public List<Task> getAllTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status);
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
        task.setTitle(textNormalizer.normalizeTitle(task.getTitle()));
        enforceTitleLength(task.getTitle());
        enforceDescriptionLength(task.getDescription());
        return taskRepository.save(task);
    }

    /**
     * Delete a task by its ID.
     * @param id
     * @return true if the task was found and deleted, false if the task didn't exist
     */
    public boolean deleteTask(Long id) {
        if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Update an existing task with partial updates.
     * @param id the ID of the task to update
     * @param title new title (optional - only updates if not null)
     * @param description new description (optional - only updates if not null)
     * @param completed new completion status (optional - only updates if not null)
     * @return the updated task
     */
    public Task updateTask(Long id, String title, String description, Boolean completed) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id " + id));
        
        // Only update fields that are provided (not null)
        if (title != null) {
            String normalized = textNormalizer.normalizeTitle(title);
            enforceTitleLength(normalized);
            task.setTitle(normalized);
        }
        if (description != null) {
            enforceDescriptionLength(description);
            task.setDescription(description);
        }
        if (completed != null) {
            task.setCompleted(completed);
        }
        
        return taskRepository.save(task);
    }

    private void enforceTitleLength(String title) {
        if (title == null) {
            return;
        }
        int max = taskProperties.getTitleMaxLength();
        if (title.length() > max) {
            throw new IllegalArgumentException("Title length exceeds max of " + max);
        }
    }

    private void enforceDescriptionLength(String description) {
        if (description == null) {
            return;
        }
        int max = taskProperties.getDescriptionMaxLength();
        if (description.length() > max) {
            throw new IllegalArgumentException("Description length exceeds max of " + max);
        }
    }
    
}
