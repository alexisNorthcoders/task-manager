package com.projects.taskmanager.graphql.input;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public class CreateTaskInput {
    @NotBlank
    @Size(max = 255)
    private String title;
    private String description;
    private Boolean completed;
    private String dueDate;
    @Min(0)
    @Max(10000)
    private Double estimationHours;

    private List<Long> assignedUserIds;

    public CreateTaskInput() {
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

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public Double getEstimationHours() {
        return estimationHours;
    }

    public void setEstimationHours(Double estimationHours) {
        this.estimationHours = estimationHours;
    }

    public List<Long> getAssignedUserIds() {
        return assignedUserIds;
    }

    public void setAssignedUserIds(List<Long> assignedUserIds) {
        this.assignedUserIds = assignedUserIds;
    }
}


