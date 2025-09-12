package com.projects.taskmanager.graphql.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateTaskCommentInput {
    
    @NotNull(message = "Task ID is required")
    private Long taskId;
    
    @NotBlank(message = "Comment content is required")
    @Size(max = 2000, message = "Comment content must not exceed 2000 characters")
    private String content;
    
    private Long parentCommentId; // For threaded comments
    
    public CreateTaskCommentInput() {
    }
    
    public CreateTaskCommentInput(Long taskId, String content) {
        this.taskId = taskId;
        this.content = content;
    }
    
    public CreateTaskCommentInput(Long taskId, String content, Long parentCommentId) {
        this.taskId = taskId;
        this.content = content;
        this.parentCommentId = parentCommentId;
    }
    
    // Getters and Setters
    public Long getTaskId() {
        return taskId;
    }
    
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Long getParentCommentId() {
        return parentCommentId;
    }
    
    public void setParentCommentId(Long parentCommentId) {
        this.parentCommentId = parentCommentId;
    }
    
    @Override
    public String toString() {
        return "CreateTaskCommentInput{" +
                "taskId=" + taskId +
                ", content='" + content + '\'' +
                ", parentCommentId=" + parentCommentId +
                '}';
    }
}
