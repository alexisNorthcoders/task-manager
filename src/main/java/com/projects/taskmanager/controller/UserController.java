package com.projects.taskmanager.controller;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import com.projects.taskmanager.model.Task;
import com.projects.taskmanager.model.User;
import com.projects.taskmanager.service.TaskService;
import com.projects.taskmanager.service.UserService;
import com.projects.taskmanager.graphql.input.CreateUserInput;
import com.projects.taskmanager.graphql.input.UpdateUserInput;
import jakarta.validation.Valid;

/**
 * GraphQL Controller for User operations.
 */
@Controller
@Validated
public class UserController {

    private final UserService userService;
    private final TaskService taskService;

    public UserController(UserService userService, TaskService taskService) {
        this.userService = userService;
        this.taskService = taskService;
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
    public User createUser(@Argument("input") @Valid CreateUserInput input) {
        User user = new User();
        user.setUsername(input.getUsername());
        user.setEmail(input.getEmail());
        user.setFirstName(input.getFirstName());
        user.setLastName(input.getLastName());
        return userService.createUser(user);
    }

    @MutationMapping
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
    public Boolean deleteUser(@Argument Long id) {
        return userService.deleteUser(id);
    }

    @MutationMapping
    public Task assignUsersToTask(@Argument Long taskId, @Argument List<Long> userIds) {
        Set<Long> userIdSet = userIds.stream().collect(Collectors.toSet());
        return userService.assignUsersToTask(taskId, userIdSet);
    }

    @MutationMapping
    public Task unassignUsersFromTask(@Argument Long taskId, @Argument List<Long> userIds) {
        Set<Long> userIdSet = userIds.stream().collect(Collectors.toSet());
        return userService.unassignUsersFromTask(taskId, userIdSet);
    }
}
