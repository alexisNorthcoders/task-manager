package com.projects.taskmanager.service;

import com.projects.taskmanager.model.TaskComment;
import com.projects.taskmanager.model.Task;
import com.projects.taskmanager.model.User;
import com.projects.taskmanager.model.ActivityType;
import com.projects.taskmanager.repository.TaskCommentRepository;
import com.projects.taskmanager.repository.TaskRepository;
import com.projects.taskmanager.repository.UserRepository;
import com.projects.taskmanager.graphql.input.CreateTaskCommentInput;
import com.projects.taskmanager.graphql.input.UpdateTaskCommentInput;
import com.projects.taskmanager.observability.MetricsService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TaskCommentService {
    
    private final TaskCommentRepository taskCommentRepository;
    private final TaskRepository taskRepository;
    private final TaskActivityService taskActivityService;
    private final MetricsService metricsService;
    
    public TaskCommentService(TaskCommentRepository taskCommentRepository, 
                            TaskRepository taskRepository,
                            TaskActivityService taskActivityService,
                            MetricsService metricsService) {
        this.taskCommentRepository = taskCommentRepository;
        this.taskRepository = taskRepository;
        this.taskActivityService = taskActivityService;
        this.metricsService = metricsService;
    }
    
    @PreAuthorize("hasRole('USER')")
    public TaskComment createComment(CreateTaskCommentInput input, User author) {
        Task task = taskRepository.findById(input.getTaskId())
            .orElseThrow(() -> new RuntimeException("Task not found with id: " + input.getTaskId()));
        
        TaskComment comment = new TaskComment();
        comment.setTask(task);
        comment.setAuthor(author);
        comment.setContent(input.getContent());
        
        // Handle threaded comments
        if (input.getParentCommentId() != null) {
            TaskComment parentComment = taskCommentRepository.findById(input.getParentCommentId())
                .orElseThrow(() -> new RuntimeException("Parent comment not found with id: " + input.getParentCommentId()));
            comment.setParentComment(parentComment);
        }
        
        TaskComment savedComment = taskCommentRepository.save(comment);
        
        // Log activity
        taskActivityService.logActivity(task, author, ActivityType.COMMENT_ADDED, 
            "Added a comment: " + (input.getParentCommentId() != null ? "reply" : "comment"));
        
        metricsService.incrementCommentCreated();
        
        return savedComment;
    }
    
    @PreAuthorize("hasRole('USER')")
    public TaskComment updateComment(Long commentId, UpdateTaskCommentInput input, User user) {
        TaskComment comment = taskCommentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));
        
        // Check if user is the author or has admin role
        if (!comment.getAuthor().getId().equals(user.getId()) && !user.getRole().name().equals("ADMIN")) {
            throw new RuntimeException("You can only edit your own comments");
        }
        
        comment.setContent(input.getContent());
        
        TaskComment updatedComment = taskCommentRepository.save(comment);
        
        // Log activity
        taskActivityService.logActivity(comment.getTask(), user, ActivityType.COMMENT_UPDATED, 
            "Updated comment content");
        
        metricsService.incrementCommentUpdated();
        
        return updatedComment;
    }
    
    @PreAuthorize("hasRole('USER')")
    public boolean deleteComment(Long commentId, User user) {
        TaskComment comment = taskCommentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));
        
        // Check if user is the author or has admin role
        if (!comment.getAuthor().getId().equals(user.getId()) && !user.getRole().name().equals("ADMIN")) {
            throw new RuntimeException("You can only delete your own comments");
        }
        
        Task task = comment.getTask();
        
        // Delete child comments first
        List<TaskComment> childComments = taskCommentRepository.findByParentCommentIdOrderByCreatedAtAsc(commentId);
        for (TaskComment child : childComments) {
            taskCommentRepository.delete(child);
        }
        
        taskCommentRepository.delete(comment);
        
        // Log activity
        taskActivityService.logActivity(task, user, ActivityType.COMMENT_DELETED, 
            "Deleted a comment");
        
        metricsService.incrementCommentDeleted();
        
        return true;
    }
    
    @Transactional(readOnly = true)
    public List<TaskComment> getCommentsByTaskId(Long taskId) {
        return taskCommentRepository.findByTaskIdOrderByCreatedAtAsc(taskId);
    }
    
    @Transactional(readOnly = true)
    public List<TaskComment> getTopLevelCommentsByTaskId(Long taskId) {
        return taskCommentRepository.findTopLevelCommentsByTaskId(taskId);
    }
    
    @Transactional(readOnly = true)
    public List<TaskComment> getRepliesByCommentId(Long commentId) {
        return taskCommentRepository.findByParentCommentIdOrderByCreatedAtAsc(commentId);
    }
    
    @Transactional(readOnly = true)
    public Optional<TaskComment> getCommentById(Long commentId) {
        return taskCommentRepository.findById(commentId);
    }
    
    @Transactional(readOnly = true)
    public long getCommentCountByTaskId(Long taskId) {
        return taskCommentRepository.countByTaskId(taskId);
    }
}
