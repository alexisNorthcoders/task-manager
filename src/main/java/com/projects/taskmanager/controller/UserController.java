package com.projects.taskmanager.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import com.projects.taskmanager.model.Task;
import com.projects.taskmanager.model.User;
import com.projects.taskmanager.repository.UserRepository;
import com.projects.taskmanager.service.TaskService;
import com.projects.taskmanager.service.UserService;
import com.projects.taskmanager.graphql.input.CreateUserInput;
import com.projects.taskmanager.graphql.input.UpdateUserInput;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * GraphQL Controller for User operations.
 */
@Controller
@Validated
public class UserController {

    private final UserService userService;
    private final TaskService taskService;
    private final UserRepository userRepository;

    public UserController(UserService userService, TaskService taskService, UserRepository userRepository) {
        this.userService = userService;
        this.taskService = taskService;
        this.userRepository = userRepository;
    }

    @QueryMapping
    public List<User> users() {
        return userService.getAllUsers();
    }

    @QueryMapping
    public User user(@Argument Long id) {
        return userService.getUserById(id).orElse(null);
    }

    @QueryMapping
    public List<User> usersByTask(@Argument Long taskId) {
        return userService.getUsersByTaskId(taskId);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public User createUser(@Argument("input") @Valid CreateUserInput input) {
        User user = new User();
        user.setUsername(input.getUsername());
        user.setEmail(input.getEmail());
        user.setFirstName(input.getFirstName());
        user.setLastName(input.getLastName());
        return userService.createUser(user);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public User updateUser(@Argument Long id, @Argument("input") @Valid UpdateUserInput input) {
        return userService.updateUser(
            id,
            input.getUsername(),
            input.getEmail(),
            input.getFirstName(),
            input.getLastName()
        );
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean deleteUser(@Argument Long id) {
        return userService.deleteUser(id);
    }

    @MutationMapping
    @PreAuthorize("hasRole('USER')")
    public Task assignUsersToTask(@Argument Long taskId, @Argument List<Long> userIds) {
        Set<Long> userIdSet = userIds.stream().collect(Collectors.toSet());
        return userService.assignUsersToTask(taskId, userIdSet);
    }

    @MutationMapping
    @PreAuthorize("hasRole('USER')")
    public Task unassignUsersFromTask(@Argument Long taskId, @Argument List<Long> userIds) {
        Set<Long> userIdSet = userIds.stream().collect(Collectors.toSet());
        return userService.unassignUsersFromTask(taskId, userIdSet);
    }

    /**
     * Batch mapping to efficiently load users for multiple tasks.
     * Spring GraphQL automatically handles the DataLoader for this.
     */
    @BatchMapping(typeName = "Task", field = "assignedUsers")
    public Map<Task, List<User>> assignedUsers(List<Task> tasks) {
        // Extract task IDs
        List<Long> taskIds = tasks.stream()
                .map(Task::getId)
                .toList();
        
        // Batch load users for all tasks
        List<User> users = userRepository.findUsersByAssignedTaskIds(taskIds);
        
        // Group users by task
        return users.stream()
                .flatMap(user -> user.getAssignedTasks().stream()
                        .filter(task -> taskIds.contains(task.getId()))
                        .map(task -> Map.entry(task, user)))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));
    }
}
