package com.projects.taskmanager.model;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import jakarta.persistence.EntityListeners;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class TaskComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    @JoinColumn(name = "parent_comment_id")
    private TaskComment parentComment;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    // Constructors
    public TaskComment() {
    }

    public TaskComment(Task task, User author, String content) {
        this.task = task;
        this.author = author;
        this.content = content;
    }

    public TaskComment(Task task, User author, String content, TaskComment parentComment) {
        this.task = task;
        this.author = author;
        this.content = content;
        this.parentComment = parentComment;
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

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public TaskComment getParentComment() {
        return parentComment;
    }

    public void setParentComment(TaskComment parentComment) {
        this.parentComment = parentComment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TaskComment other = (TaskComment) obj;
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
        return "TaskComment{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", author=" + (author != null ? author.getUsername() : "null") +
                '}';
    }
}
