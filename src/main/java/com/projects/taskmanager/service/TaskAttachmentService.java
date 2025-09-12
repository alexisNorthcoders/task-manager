package com.projects.taskmanager.service;

import com.projects.taskmanager.model.TaskAttachment;
import com.projects.taskmanager.model.Task;
import com.projects.taskmanager.model.User;
import com.projects.taskmanager.model.ActivityType;
import com.projects.taskmanager.repository.TaskAttachmentRepository;
import com.projects.taskmanager.repository.TaskRepository;
import com.projects.taskmanager.observability.MetricsService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class TaskAttachmentService {
    
    private final TaskAttachmentRepository taskAttachmentRepository;
    private final TaskRepository taskRepository;
    private final TaskActivityService taskActivityService;
    private final MetricsService metricsService;
    
    // Configuration for file uploads
    private static final String UPLOAD_DIR = "uploads/attachments";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
        "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    private static final List<String> ALLOWED_DOCUMENT_TYPES = List.of(
        "application/pdf", "text/plain", "application/msword", 
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );
    
    public TaskAttachmentService(TaskAttachmentRepository taskAttachmentRepository,
                               TaskRepository taskRepository,
                               TaskActivityService taskActivityService,
                               MetricsService metricsService) {
        this.taskAttachmentRepository = taskAttachmentRepository;
        this.taskRepository = taskRepository;
        this.taskActivityService = taskActivityService;
        this.metricsService = metricsService;
    }
    
    @PreAuthorize("hasRole('USER')")
    public TaskAttachment uploadAttachment(Long taskId, MultipartFile file, String description, User uploader) throws IOException {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
        
        // Validate file
        validateFile(file);
        
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        Path filePath = uploadPath.resolve(uniqueFilename);
        
        // Save file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Create attachment record
        TaskAttachment attachment = new TaskAttachment();
        attachment.setTask(task);
        attachment.setUploader(uploader);
        attachment.setFilename(uniqueFilename);
        attachment.setOriginalFilename(originalFilename);
        attachment.setContentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
        attachment.setFileSize(file.getSize());
        attachment.setFilePath(filePath.toString());
        attachment.setDescription(description);
        
        TaskAttachment savedAttachment = taskAttachmentRepository.save(attachment);
        
        // Log activity
        String activityDescription = "Uploaded attachment: " + originalFilename;
        if (attachment.isImage()) {
            activityDescription += " (image)";
        }
        taskActivityService.logActivity(task, uploader, ActivityType.ATTACHMENT_ADDED, activityDescription);
        
        metricsService.incrementAttachmentUploaded();
        
        return savedAttachment;
    }
    
    @PreAuthorize("hasRole('USER')")
    public boolean deleteAttachment(Long attachmentId, User user) {
        TaskAttachment attachment = taskAttachmentRepository.findById(attachmentId)
            .orElseThrow(() -> new RuntimeException("Attachment not found with id: " + attachmentId));
        
        // Check if user is the uploader or has admin role
        if (!attachment.getUploader().getId().equals(user.getId()) && !user.getRole().name().equals("ADMIN")) {
            throw new RuntimeException("You can only delete your own attachments");
        }
        
        Task task = attachment.getTask();
        String filename = attachment.getOriginalFilename();
        
        try {
            // Delete file from filesystem
            Path filePath = Paths.get(attachment.getFilePath());
            Files.deleteIfExists(filePath);
            
            // Delete database record
            taskAttachmentRepository.delete(attachment);
            
            // Log activity
            taskActivityService.logActivity(task, user, ActivityType.ATTACHMENT_DELETED, 
                "Deleted attachment: " + filename);
            
            metricsService.incrementAttachmentDeleted();
            
            return true;
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete attachment file", e);
        }
    }
    
    @Transactional(readOnly = true)
    public List<TaskAttachment> getAttachmentsByTaskId(Long taskId) {
        return taskAttachmentRepository.findByTaskIdOrderByCreatedAtDesc(taskId);
    }
    
    @Transactional(readOnly = true)
    public List<TaskAttachment> getImagesByTaskId(Long taskId) {
        return taskAttachmentRepository.findImagesByTaskId(taskId);
    }
    
    @Transactional(readOnly = true)
    public List<TaskAttachment> getNonImagesByTaskId(Long taskId) {
        return taskAttachmentRepository.findNonImagesByTaskId(taskId);
    }
    
    @Transactional(readOnly = true)
    public Optional<TaskAttachment> getAttachmentById(Long attachmentId) {
        return taskAttachmentRepository.findById(attachmentId);
    }
    
    @Transactional(readOnly = true)
    public long getAttachmentCountByTaskId(Long taskId) {
        return taskAttachmentRepository.countByTaskId(taskId);
    }
    
    @Transactional(readOnly = true)
    public long getImageCountByTaskId(Long taskId) {
        return taskAttachmentRepository.countByTaskIdAndContentTypeStartingWith(taskId, "image/");
    }
    
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("File size exceeds maximum allowed size of " + (MAX_FILE_SIZE / 1024 / 1024) + "MB");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || (!ALLOWED_IMAGE_TYPES.contains(contentType) && !ALLOWED_DOCUMENT_TYPES.contains(contentType))) {
            throw new RuntimeException("File type not allowed. Allowed types: images (JPEG, PNG, GIF, WebP) and documents (PDF, TXT, DOC, DOCX)");
        }
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
