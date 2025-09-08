package com.projects.taskmanager.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.projects.taskmanager.model.Task;
import com.projects.taskmanager.model.User;
import com.projects.taskmanager.repository.TaskRepository;
import com.projects.taskmanager.repository.UserRepository;
import com.projects.taskmanager.service.exception.TaskNotFoundException;
import com.projects.taskmanager.service.exception.UserNotFoundException;
import com.projects.taskmanager.graphql.BulkOperationResult;
import java.util.ArrayList;

/**
 * Service for managing users.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final WebSocketNotificationService notificationService;

    /**
     * Constructor for UserService.
     * @param userRepository the repository to use for user operations
     * @param taskRepository the repository to use for task operations
     */
    public UserService(UserRepository userRepository, TaskRepository taskRepository, WebSocketNotificationService notificationService) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.notificationService = notificationService;
    }

    /**
     * Get all users from the database.
     * @return list of users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get a user by its ID.
     * @param id the user ID
     * @return the user
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Get a user by username.
     * @param username the username
     * @return the user
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Get a user by email.
     * @param email the email
     * @return the user
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Create a new user.
     * @param user the user to create
     * @return the created user
     */
    public User createUser(User user) {
        // Check for duplicate username
        if (userRepository.findByUsername(user.getUsername()) != null) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }

        // Check for duplicate email
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }

        return userRepository.save(user);
    }

    /**
     * Update an existing user.
     * @param id the ID of the user to update
     * @param username new username (optional)
     * @param email new email (optional)
     * @param firstName new first name (optional)
     * @param lastName new last name (optional)
     * @return the updated user
     */
    public User updateUser(Long id, String username, String email, String firstName, String lastName) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        // Update fields only if provided
        if (username != null && !username.equals(user.getUsername())) {
            if (userRepository.findByUsername(username) != null) {
                throw new IllegalArgumentException("Username already exists: " + username);
            }
            user.setUsername(username);
        }

        if (email != null && !email.equals(user.getEmail())) {
            if (userRepository.findByEmail(email) != null) {
                throw new IllegalArgumentException("Email already exists: " + email);
            }
            user.setEmail(email);
        }

        if (firstName != null) {
            user.setFirstName(firstName);
        }

        if (lastName != null) {
            user.setLastName(lastName);
        }

        return userRepository.save(user);
    }

    /**
     * Delete a user by its ID.
     * @param id the user ID
     * @return true if the user was found and deleted, false if the user didn't exist
     */
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Assign users to a task.
     * @param taskId the task ID
     * @param userIds the user IDs to assign
     * @return the updated task
     */
    public Task assignUsersToTask(Long taskId, Set<Long> userIds) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        // Clear existing assignments and assign new ones
        task.getAssignedUsers().clear();

        for (Long userId : userIds) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException(userId));
            task.assignUser(user);
        }

        Task savedTask = taskRepository.save(task);
        
        // Send WebSocket notification to assigned users
        notificationService.notifyAssignedUsers(savedTask, "TASK_ASSIGNED");
        notificationService.notifyTaskUpdated(savedTask);
        
        return savedTask;
    }

    /**
     * Unassign users from a task.
     * @param taskId the task ID
     * @param userIds the user IDs to unassign
     * @return the updated task
     */
    public Task unassignUsersFromTask(Long taskId, Set<Long> userIds) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        for (Long userId : userIds) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException(userId));
            task.unassignUser(user);
        }

        Task savedTask = taskRepository.save(task);
        
        // Send WebSocket notification
        notificationService.notifyTaskUpdated(savedTask);
        
        return savedTask;
    }

    /**
     * Get users assigned to a specific task.
     * @param taskId the task ID
     * @return list of assigned users
     */
    public List<User> getUsersByTaskId(Long taskId) {
        return userRepository.findUsersByTaskId(taskId);
    }

    /**
     * Bulk assign users to multiple tasks.
     * @param taskIds the task IDs
     * @param userIds the user IDs to assign
     * @return bulk operation result
     */
    public BulkOperationResult bulkAssignUsers(List<Long> taskIds, List<Long> userIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return new BulkOperationResult(false, 0, List.of("No task IDs provided"));
        }
        if (userIds == null || userIds.isEmpty()) {
            return new BulkOperationResult(false, 0, List.of("No user IDs provided"));
        }

        List<String> errors = new ArrayList<>();
        int updatedCount = 0;

        // Validate all users exist first
        for (Long userId : userIds) {
            if (!userRepository.existsById(userId)) {
                errors.add("User not found: " + userId);
            }
        }

        if (!errors.isEmpty()) {
            return new BulkOperationResult(false, 0, errors);
        }

        // Process each task
        for (Long taskId : taskIds) {
            try {
                assignUsersToTask(taskId, Set.copyOf(userIds));
                updatedCount++;
            } catch (Exception e) {
                errors.add("Failed to assign users to task " + taskId + ": " + e.getMessage());
            }
        }

        boolean success = errors.isEmpty();
        BulkOperationResult result = new BulkOperationResult(success, updatedCount, errors);
        
        // Send WebSocket notification
        if (updatedCount > 0) {
            notificationService.notifyBulkOperation("BULK_ASSIGN", updatedCount);
        }
        
        return result;
    }
}
