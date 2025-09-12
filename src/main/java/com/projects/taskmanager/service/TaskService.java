package com.projects.taskmanager.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.projects.taskmanager.config.TaskProperties;
import com.projects.taskmanager.model.Task;
import com.projects.taskmanager.model.TaskStatus;
import com.projects.taskmanager.model.User;
import com.projects.taskmanager.model.ActivityType;
import com.projects.taskmanager.repository.TaskRepository;
import com.projects.taskmanager.util.TextNormalizer;
import com.projects.taskmanager.service.exception.TaskNotFoundException;
import com.projects.taskmanager.service.exception.UserNotFoundException;
import com.projects.taskmanager.graphql.BulkOperationResult;
import com.projects.taskmanager.graphql.input.BulkUpdateTaskInput;
import java.util.Set;
import java.util.ArrayList;

/**
 * Service for managing tasks.
 */
@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskProperties taskProperties;
    private final TextNormalizer textNormalizer;
    private final WebSocketNotificationService notificationService;
    private final TaskActivityService taskActivityService;

    /**
     * Constructor for TaskService.
     * @param taskRepository the repository to use for task operations
     */
    public TaskService(TaskRepository taskRepository, TaskProperties taskProperties, TextNormalizer textNormalizer, WebSocketNotificationService notificationService, TaskActivityService taskActivityService) {
        this.taskRepository = taskRepository;
        this.taskProperties = taskProperties;
        this.textNormalizer = textNormalizer;
        this.notificationService = notificationService;
        this.taskActivityService = taskActivityService;
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
        Task savedTask = taskRepository.save(task);
        
        // Log activity (if we have a user context)
        // Note: In a real implementation, you'd need to pass the user context
        // For now, we'll skip activity logging in the service layer
        // and handle it in the controller layer where we have user context
        
        // Send WebSocket notification
        notificationService.notifyTaskCreated(savedTask);
        
        return savedTask;
    }

    /**
     * Delete a task by its ID.
     * @param id
     * @return true if the task was found and deleted, false if the task didn't exist
     */
    public boolean deleteTask(Long id) {
        if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
            
            // Send WebSocket notification
            notificationService.notifyTaskDeleted(id);
            
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
    public Task updateTask(Long id, String title, String description, Boolean completed, TaskStatus status, String dueDate, Double estimationHours) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        
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
        if (status != null) {
            task.setStatus(status);
        }
        if (dueDate != null) {
            task.setDueDate(parseDueDate(dueDate));
        }
        if (estimationHours != null) {
            enforceEstimationHours(estimationHours);
            task.setEstimationHours(estimationHours);
        }
        
        Task savedTask = taskRepository.save(task);
        
        // Send WebSocket notification
        notificationService.notifyTaskUpdated(savedTask);
        
        return savedTask;
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

    private LocalDate parseDueDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid dueDate format, expected YYYY-MM-DD");
        }
    }

    private void enforceEstimationHours(Double estimationHours) {
        if (estimationHours == null) {
            return;
        }
        if (estimationHours < 0) {
            throw new IllegalArgumentException("Estimation hours must be >= 0");
        }
        if (estimationHours > 10000) {
            throw new IllegalArgumentException("Estimation hours is unreasonably large");
        }
    }

    /**
     * Get paginated tasks with optional filtering and sorting.
     * @param page page number (0-based)
     * @param size page size
     * @param completed filter by completion status (optional)
     * @param titleContains filter by title containing text (optional)
     * @param sortBy sort field (optional)
     * @param sortDirection sort direction (optional, defaults to ASC)
     * @return paginated result
     */
    public Page<Task> getTasksPaginated(
            int page,
            int size,
            Boolean completed,
            String titleContains,
            String sortBy,
            String sortDirection) {

        // Validate page parameters
        if (page < 0) {
            throw new IllegalArgumentException("Page number must be >= 0");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }

        // Create pageable with sorting
        Sort sort = createSort(sortBy, sortDirection);
        Pageable pageable = PageRequest.of(page, size, sort);

        // Use repository method with filters
        return taskRepository.findTasksWithFilters(completed, titleContains, pageable);
    }

    /**
     * Get paginated tasks with advanced filtering options.
     * @param page page number (0-based)
     * @param size page size
     * @param completed filter by completion status (optional)
     * @param titleContains filter by title containing text (optional)
     * @param status filter by task status (optional)
     * @param dueDateFrom filter by minimum due date (optional)
     * @param dueDateTo filter by maximum due date (optional)
     * @param estimationHoursMin filter by minimum estimation hours (optional)
     * @param estimationHoursMax filter by maximum estimation hours (optional)
     * @param sortBy sort field (optional)
     * @param sortDirection sort direction (optional, defaults to ASC)
     * @return paginated result
     */
    public Page<Task> getTasksAdvancedFiltered(
            int page,
            int size,
            Boolean completed,
            String titleContains,
            TaskStatus status,
            LocalDate dueDateFrom,
            LocalDate dueDateTo,
            Double estimationHoursMin,
            Double estimationHoursMax,
            String sortBy,
            String sortDirection) {

        // Validate page parameters
        if (page < 0) {
            throw new IllegalArgumentException("Page number must be >= 0");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }

        // Create pageable with sorting
        Sort sort = createSort(sortBy, sortDirection);
        Pageable pageable = PageRequest.of(page, size, sort);

        // Use repository method with advanced filters
        return taskRepository.findTasksWithAdvancedFilters(
            completed, titleContains, status, dueDateFrom, dueDateTo,
            estimationHoursMin, estimationHoursMax, pageable);
    }

    /**
     * Create a Sort object based on sortBy and sortDirection parameters.
     * @param sortBy the field to sort by
     * @param sortDirection the sort direction (ASC or DESC)
     * @return Sort object
     */
    private Sort createSort(String sortBy, String sortDirection) {
        if (sortBy == null) {
            return Sort.by("id").ascending(); // Default sort
        }

        Sort.Direction direction = "DESC".equalsIgnoreCase(sortDirection)
            ? Sort.Direction.DESC
            : Sort.Direction.ASC;

        return switch (sortBy.toUpperCase()) {
            case "TITLE" -> Sort.by(direction, "title");
            case "CREATED_AT" -> Sort.by(direction, "createdAt");
            case "UPDATED_AT" -> Sort.by(direction, "updatedAt");
            case "DUE_DATE" -> Sort.by(direction, "dueDate");
            case "STATUS" -> Sort.by(direction, "status");
            default -> Sort.by("id").ascending(); // Default sort
        };
    }

    /**
     * Bulk update multiple tasks with the same values.
     * @param taskIds list of task IDs to update
     * @param input bulk update input with new values
     * @return bulk operation result
     */
    public BulkOperationResult bulkUpdateTasks(List<Long> taskIds, BulkUpdateTaskInput input) {
        if (taskIds == null || taskIds.isEmpty()) {
            return new BulkOperationResult(false, 0, List.of("No task IDs provided"));
        }

        List<String> errors = new ArrayList<>();
        int updatedCount = 0;

        for (Long taskId : taskIds) {
            try {
                Task task = taskRepository.findById(taskId)
                        .orElseThrow(() -> new TaskNotFoundException(taskId));

                // Update fields if provided
                if (input.getStatus() != null) {
                    task.setStatus(input.getStatus());
                }
                if (input.getCompleted() != null) {
                    task.setCompleted(input.getCompleted());
                }
                if (input.getDueDate() != null) {
                    task.setDueDate(parseDueDate(input.getDueDate()));
                }

                taskRepository.save(task);
                updatedCount++;
            } catch (Exception e) {
                errors.add("Failed to update task " + taskId + ": " + e.getMessage());
            }
        }

        boolean success = errors.isEmpty();
        BulkOperationResult result = new BulkOperationResult(success, updatedCount, errors);
        
        // Send WebSocket notification
        if (updatedCount > 0) {
            notificationService.notifyBulkOperation("BULK_UPDATE", updatedCount);
        }
        
        return result;
    }

    /**
     * Bulk delete multiple tasks.
     * @param taskIds list of task IDs to delete
     * @return bulk operation result
     */
    public BulkOperationResult bulkDeleteTasks(List<Long> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return new BulkOperationResult(false, 0, List.of("No task IDs provided"));
        }

        List<String> errors = new ArrayList<>();
        int deletedCount = 0;

        for (Long taskId : taskIds) {
            try {
                if (taskRepository.existsById(taskId)) {
                    taskRepository.deleteById(taskId);
                    deletedCount++;
                } else {
                    errors.add("Task not found: " + taskId);
                }
            } catch (Exception e) {
                errors.add("Failed to delete task " + taskId + ": " + e.getMessage());
            }
        }

        boolean success = errors.isEmpty();
        BulkOperationResult result = new BulkOperationResult(success, deletedCount, errors);
        
        // Send WebSocket notification
        if (deletedCount > 0) {
            notificationService.notifyBulkOperation("BULK_DELETE", deletedCount);
        }
        
        return result;
    }

}
