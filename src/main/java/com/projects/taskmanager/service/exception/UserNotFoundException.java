package com.projects.taskmanager.service.exception;

/**
 * Exception thrown when a user is not found.
 */
public class UserNotFoundException extends RuntimeException {

    private final Long userId;

    public UserNotFoundException(Long userId) {
        super("User not found with id: " + userId);
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }
}
