package com.projects.taskmanager.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.projects.taskmanager.model.Task;
import com.projects.taskmanager.model.TaskTemplate;
import com.projects.taskmanager.repository.TaskTemplateRepository;
import com.projects.taskmanager.repository.TaskRepository;
import com.projects.taskmanager.service.exception.TaskTemplateNotFoundException;
import com.projects.taskmanager.util.TextNormalizer;

@Service
public class TaskTemplateService {

    private final TaskTemplateRepository templateRepository;
    private final TaskRepository taskRepository;
    private final TextNormalizer textNormalizer;
    private final UserService userService;
    private final WebSocketNotificationService notificationService;

    public TaskTemplateService(TaskTemplateRepository templateRepository, 
                             TaskRepository taskRepository,
                             TextNormalizer textNormalizer,
                             UserService userService,
                             WebSocketNotificationService notificationService) {
        this.templateRepository = templateRepository;
        this.taskRepository = taskRepository;
        this.textNormalizer = textNormalizer;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    /**
     * Get all task templates
     */
    public List<TaskTemplate> getAllTaskTemplates() {
        return templateRepository.findAllByOrderByNameAsc();
    }

    /**
     * Get a task template by ID
     */
    public Optional<TaskTemplate> getTaskTemplateById(Long id) {
        return templateRepository.findById(id);
    }

    /**
     * Create a new task template
     */
    public TaskTemplate createTaskTemplate(TaskTemplate template) {
        // Normalize the name and title
        template.setName(textNormalizer.normalizeTitle(template.getName()));
        template.setTitle(textNormalizer.normalizeTitle(template.getTitle()));
        
        // Check if template name already exists
        if (templateRepository.existsByNameIgnoreCase(template.getName())) {
            throw new IllegalArgumentException("Template with name '" + template.getName() + "' already exists");
        }
        
        return templateRepository.save(template);
    }

    /**
     * Update an existing task template
     */
    public TaskTemplate updateTaskTemplate(Long id, String name, String title, String description, Double estimationHours) {
        TaskTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new TaskTemplateNotFoundException(id));

        // Update fields only if provided
        if (name != null) {
            String normalizedName = textNormalizer.normalizeTitle(name);
            // Check if new name conflicts with existing templates (excluding current one)
            if (!template.getName().equals(normalizedName) && 
                templateRepository.existsByNameIgnoreCase(normalizedName)) {
                throw new IllegalArgumentException("Template with name '" + normalizedName + "' already exists");
            }
            template.setName(normalizedName);
        }
        
        if (title != null) {
            template.setTitle(textNormalizer.normalizeTitle(title));
        }
        
        if (description != null) {
            template.setDescription(description);
        }
        
        if (estimationHours != null) {
            if (estimationHours < 0 || estimationHours > 10000) {
                throw new IllegalArgumentException("Estimation hours must be between 0 and 10000");
            }
            template.setEstimationHours(estimationHours);
        }

        return templateRepository.save(template);
    }

    /**
     * Delete a task template
     */
    public boolean deleteTaskTemplate(Long id) {
        if (templateRepository.existsById(id)) {
            templateRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Create a new task from a template
     */
    public Task createTaskFromTemplate(Long templateId, List<Long> assignedUserIds) {
        TaskTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new TaskTemplateNotFoundException(templateId));

        // Create new task from template
        Task task = new Task();
        task.setTitle(template.getTitle());
        task.setDescription(template.getDescription());
        task.setEstimationHours(template.getEstimationHours());
        task.setCompleted(false); // New tasks are not completed

        // Save the task
        Task savedTask = taskRepository.save(task);

        // Assign users if provided
        if (assignedUserIds != null && !assignedUserIds.isEmpty()) {
            savedTask = userService.assignUsersToTask(savedTask.getId(), Set.copyOf(assignedUserIds));
        }

        // Send WebSocket notification
        notificationService.notifyTaskCreated(savedTask);

        return savedTask;
    }

    /**
     * Search templates by name
     */
    public List<TaskTemplate> searchTemplatesByName(String name) {
        return templateRepository.findByNameContainingIgnoreCase(name);
    }
}