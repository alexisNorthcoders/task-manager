package com.projects.taskmanager.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import jakarta.persistence.EntityListeners;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private boolean completed;
    
    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.TODO;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private LocalDate dueDate;

    private Double estimationHours; // optional estimate in hours

    @ManyToMany(fetch = jakarta.persistence.FetchType.EAGER)
    @JoinTable(
        name = "task_users",
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id")
    )
    private Set<User> assignedUsers = new HashSet<>();

    // Constructors
    public Task() {
    }

    public Task(String title, String description, boolean completed) {
        this.title = title;
        this.description = description;
        this.completed = completed;
        this.status = completed ? TaskStatus.DONE : TaskStatus.TODO;
    }

    public static Task create(String title, String description) {
        return new Task(title, description, false);
    }

    // Getters and Setters
    public Long getId() {
        return id;
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

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
        // keep status in sync
        if (completed) {
            status = TaskStatus.DONE;
        } else if (status == null || status == TaskStatus.DONE) {
            status = TaskStatus.TODO;
        }
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
        // keep completed in sync
        this.completed = (status == TaskStatus.DONE);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Double getEstimationHours() {
        return estimationHours;
    }

    public void setEstimationHours(Double estimationHours) {
        this.estimationHours = estimationHours;
    }

    public Set<User> getAssignedUsers() {
        if (assignedUsers == null) {
            assignedUsers = new HashSet<>();
        }
        return assignedUsers;
    }

    public void setAssignedUsers(Set<User> assignedUsers) {
        this.assignedUsers = assignedUsers;
    }

    // Helper methods for managing user assignments
    public void assignUser(User user) {
        this.assignedUsers.add(user);
        user.getAssignedTasks().add(this);
    }

    public void unassignUser(User user) {
        this.assignedUsers.remove(user);
        user.getAssignedTasks().remove(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Task other = (Task) obj;
        if (this.id != null && other.id != null) {
            return this.id.equals(other.id);
        }
        // If either id is null (not persisted), treat as not equal unless same instance
        return false;
    }

    @Override
    public int hashCode() {
        // Use id hash when available; otherwise, a stable class-based hash per JPA best practices
        return (id != null) ? id.hashCode() : getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", completed=" + completed +
                '}';
    }
}