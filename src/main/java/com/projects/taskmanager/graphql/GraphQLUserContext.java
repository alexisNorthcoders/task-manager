package com.projects.taskmanager.graphql;

import com.projects.taskmanager.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * GraphQL context for accessing authenticated user information
 */
@Component
public class GraphQLUserContext {

    /**
     * Get the currently authenticated user
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * Check if user is authenticated
     */
    public boolean isAuthenticated() {
        return getCurrentUser() != null;
    }

    /**
     * Check if current user has admin role
     */
    public boolean isAdmin() {
        User user = getCurrentUser();
        return user != null && user.getRole().name().equals("ADMIN");
    }

    /**
     * Get current user's ID
     */
    public Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }
}
