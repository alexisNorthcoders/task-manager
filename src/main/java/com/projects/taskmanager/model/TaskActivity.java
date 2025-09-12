package com.projects.taskmanager.model;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import jakarta.persistence.EntityListeners;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class TaskActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType activityType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String oldValue;

    @Column(columnDefinition = "TEXT")
    private String newValue;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    // Constructors
    public TaskActivity() {
    }

    public TaskActivity(Task task, User user, ActivityType activityType, String description) {
        this.task = task;
        this.user = user;
        this.activityType = activityType;
        this.description = description;
    }

    public TaskActivity(Task task, User user, ActivityType activityType, String description, String oldValue, String newValue) {
        this.task = task;
        this.user = user;
        this.activityType = activityType;
        this.description = description;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public void setActivityType(ActivityType activityType) {
        this.activityType = activityType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TaskActivity other = (TaskActivity) obj;
        if (this.id != null && other.id != null) {
            return this.id.equals(other.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (id != null) ? id.hashCode() : getClass().hashCode();
    }

    @Override
    public String toString() {
        return "TaskActivity{" +
                "id=" + id +
                ", activityType=" + activityType +
                ", description='" + description + '\'' +
                ", user=" + (user != null ? user.getUsername() : "null") +
                '}';
    }
}
