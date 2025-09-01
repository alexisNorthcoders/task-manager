package com.projects.taskmanager.graphql.input;

public class CreateTaskInput {
    private String title;
    private String description;
    private Boolean completed;
    private String dueDate;
    private Integer estimationHours;

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

    public Integer getEstimationHours() {
        return estimationHours;
    }

    public void setEstimationHours(Integer estimationHours) {
        this.estimationHours = estimationHours;
    }
}


