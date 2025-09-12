package com.projects.taskmanager.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;

import com.projects.taskmanager.model.TaskAttachment;
import com.projects.taskmanager.model.User;
import com.projects.taskmanager.service.TaskAttachmentService;
import com.projects.taskmanager.service.UserService;
import com.projects.taskmanager.observability.MetricsService;
import com.projects.taskmanager.dto.TaskAttachmentResponse;
import io.micrometer.core.instrument.Timer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/attachments")
public class FileUploadController {
    
    private final TaskAttachmentService taskAttachmentService;
    private final UserService userService;
    private final MetricsService metricsService;
    
    public FileUploadController(TaskAttachmentService taskAttachmentService,
                              UserService userService,
                              MetricsService metricsService) {
        this.taskAttachmentService = taskAttachmentService;
        this.userService = userService;
        this.metricsService = metricsService;
    }
    
    @PostMapping("/upload")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> uploadAttachment(
            @RequestParam("taskId") Long taskId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description) {
        
        Timer.Sample sample = metricsService.startGraphQLMutationTimer();
        try {
            // Get current user from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User currentUser = userService.getUserByUsername(username);
            
            TaskAttachment attachment = taskAttachmentService.uploadAttachment(taskId, file, description, currentUser);
            
            // Convert to DTO to avoid lazy loading issues
            TaskAttachmentResponse attachmentResponse = new TaskAttachmentResponse(
                attachment.getId(),
                attachment.getFilename(),
                attachment.getOriginalFilename(),
                attachment.getContentType(),
                attachment.getFileSize(),
                attachment.getFilePath(),
                attachment.getDescription(),
                attachment.getCreatedAt(),
                attachment.getUpdatedAt(),
                attachment.isImage(),
                attachment.getFileSizeFormatted(),
                attachment.getUploader().getUsername(),
                attachment.getUploader().getFirstName(),
                attachment.getUploader().getLastName()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("attachment", attachmentResponse);
            response.put("message", "File uploaded successfully");
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to upload file: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } finally {
            metricsService.stopGraphQLMutationTimer(sample);
        }
    }
    
    @GetMapping("/download/{attachmentId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable Long attachmentId) {
        try {
            Optional<TaskAttachment> attachment = taskAttachmentService.getAttachmentById(attachmentId);
            if (attachment.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            TaskAttachment att = attachment.get();
            Path filePath = Paths.get(att.getFilePath());
            
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }
            
            byte[] fileContent = Files.readAllBytes(filePath);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(att.getContentType()));
            headers.setContentDispositionFormData("attachment", att.getOriginalFilename());
            headers.setContentLength(fileContent.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileContent);
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }
}
