package com.projects.taskmanager.graphql.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateTaskCommentInput {
    
    @NotBlank(message = "Comment content is required")
    @Size(max = 2000, message = "Comment content must not exceed 2000 characters")
    private String content;
    
    public UpdateTaskCommentInput() {
    }
    
    public UpdateTaskCommentInput(String content) {
        this.content = content;
    }
    
    // Getters and Setters
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    @Override
    public String toString() {
        return "UpdateTaskCommentInput{" +
                "content='" + content + '\'' +
                '}';
    }
}
