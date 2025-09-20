package com.projects.taskmanager.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.projects.taskmanager.model.Task;
import com.projects.taskmanager.model.User;
import com.projects.taskmanager.service.UserService;
import com.projects.taskmanager.graphql.input.CreateUserInput;
import com.projects.taskmanager.graphql.input.UpdateUserInput;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.HashMap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * GraphQL Controller for User operations.
 */
@Controller
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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

    @MutationMapping
    @PreAuthorize("hasRole('USER')")
    public User updateUserPassword(@Argument String currentPassword, @Argument String newPassword) {
        // Get current user from security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userService.getUserByUsername(username);

        return userService.updateUserPassword(currentUser.getId(), currentPassword, newPassword);
    }

    @MutationMapping
    @PreAuthorize("hasRole('USER')")
    public User updateUserAvatar(@Argument String avatarUrl) {
        // Get current user from security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userService.getUserByUsername(username);

        return userService.updateUserAvatar(currentUser.getId(), avatarUrl);
    }


    // REST endpoints for user settings

    /**
     * Update user password.
     */
    @PostMapping("/api/user/password")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> updatePassword(@RequestBody Map<String, String> request) {
        try {
            // Get current user from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User currentUser = userService.getUserByUsername(username);

            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");

            if (currentPassword == null || newPassword == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "Current password and new password are required");
                return ResponseEntity.badRequest().body(response);
            }

            // Use the UserService method we created
            userService.updateUserPassword(currentUser.getId(), currentPassword, newPassword);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Password updated successfully");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to update password: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Upload user avatar.
     */
    @PostMapping("/api/user/avatar")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "File is empty");
                return ResponseEntity.badRequest().body(response);
            }

            // Check file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "File must be an image");
                return ResponseEntity.badRequest().body(response);
            }

            // Check file size (max 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "File size must be less than 5MB");
                return ResponseEntity.badRequest().body(response);
            }

            // Get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User currentUser = userService.getUserByUsername(username);

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
            String uniqueFilename = "avatar_" + currentUser.getId() + "_" + UUID.randomUUID() + extension;

            // Create uploads directory if it doesn't exist
            Path uploadDir = Paths.get("uploads", "avatars");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Save file
            Path filePath = uploadDir.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Update user avatar URL
            String avatarUrl = "/api/user/avatar/" + uniqueFilename;
            userService.updateUserAvatar(currentUser.getId(), avatarUrl);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("avatarUrl", avatarUrl);
            response.put("message", "Avatar uploaded successfully");
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to upload avatar: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to upload avatar: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get user avatar.
     */
    @GetMapping("/api/user/avatar/{filename}")
    public ResponseEntity<byte[]> getAvatar(@PathVariable String filename) {
        try {
            Path filePath = Paths.get("uploads", "avatars", filename);

            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            byte[] fileContent = Files.readAllBytes(filePath);
            String contentType = Files.probeContentType(filePath);

            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .header("Content-Type", contentType)
                    .header("Cache-Control", "public, max-age=86400") // Cache for 1 day
                    .body(fileContent);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
