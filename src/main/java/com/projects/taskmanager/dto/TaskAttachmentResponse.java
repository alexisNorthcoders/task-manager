package com.projects.taskmanager.dto;

import java.time.Instant;

public class TaskAttachmentResponse {
    private Long id;
    private String filename;
    private String originalFilename;
    private String contentType;
    private Long fileSize;
    private String filePath;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean isImage;
    private String fileSizeFormatted;
    private String uploaderUsername;
    private String uploaderFirstName;
    private String uploaderLastName;

    public TaskAttachmentResponse() {}

    public TaskAttachmentResponse(Long id, String filename, String originalFilename, 
                                String contentType, Long fileSize, String filePath, 
                                String description, Instant createdAt, Instant updatedAt,
                                boolean isImage, String fileSizeFormatted,
                                String uploaderUsername, String uploaderFirstName, String uploaderLastName) {
        this.id = id;
        this.filename = filename;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.filePath = filePath;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isImage = isImage;
        this.fileSizeFormatted = fileSizeFormatted;
        this.uploaderUsername = uploaderUsername;
        this.uploaderFirstName = uploaderFirstName;
        this.uploaderLastName = uploaderLastName;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public boolean isImage() { return isImage; }
    public void setImage(boolean image) { isImage = image; }

    public String getFileSizeFormatted() { return fileSizeFormatted; }
    public void setFileSizeFormatted(String fileSizeFormatted) { this.fileSizeFormatted = fileSizeFormatted; }

    public String getUploaderUsername() { return uploaderUsername; }
    public void setUploaderUsername(String uploaderUsername) { this.uploaderUsername = uploaderUsername; }

    public String getUploaderFirstName() { return uploaderFirstName; }
    public void setUploaderFirstName(String uploaderFirstName) { this.uploaderFirstName = uploaderFirstName; }

    public String getUploaderLastName() { return uploaderLastName; }
    public void setUploaderLastName(String uploaderLastName) { this.uploaderLastName = uploaderLastName; }
}
