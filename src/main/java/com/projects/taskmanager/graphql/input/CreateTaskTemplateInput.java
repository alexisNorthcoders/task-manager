package com.projects.taskmanager.graphql.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class CreateTaskTemplateInput {
    
    @NotBlank(message = "Template name is required")
    @Size(max = 100, message = "Template name must be less than 100 characters")
    private String name;
    
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title;
    
    @Size(max = 2000, message = "Description must be less than 2000 characters")
    private String description;
    
    @Min(0)
    @Max(10000)
    private Double estimationHours;

    public CreateTaskTemplateInput() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getEstimationHours() {
        return estimationHours;
    }

    public void setEstimationHours(Double estimationHours) {
        this.estimationHours = estimationHours;
    }
}