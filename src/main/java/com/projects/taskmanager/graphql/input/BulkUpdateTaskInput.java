package com.projects.taskmanager.graphql.input;

import com.projects.taskmanager.model.TaskStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;

public class BulkUpdateTaskInput {
    private TaskStatus status;
    private Boolean completed;
    private List<Long> assignedUserIds;
    private String dueDate;

    public BulkUpdateTaskInput() {
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public List<Long> getAssignedUserIds() {
        return assignedUserIds;
    }

    public void setAssignedUserIds(List<Long> assignedUserIds) {
        this.assignedUserIds = assignedUserIds;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }
}