package com.projects.taskmanager.repository;

import com.projects.taskmanager.model.TaskAttachment;
import com.projects.taskmanager.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Long> {
    
    List<TaskAttachment> findByTaskOrderByCreatedAtDesc(Task task);
    
    List<TaskAttachment> findByTaskIdOrderByCreatedAtDesc(Long taskId);
    
    @Query("SELECT a FROM TaskAttachment a WHERE a.task.id = :taskId AND a.contentType LIKE 'image/%' ORDER BY a.createdAt DESC")
    List<TaskAttachment> findImagesByTaskId(@Param("taskId") Long taskId);
    
    @Query("SELECT a FROM TaskAttachment a WHERE a.task.id = :taskId AND a.contentType NOT LIKE 'image/%' ORDER BY a.createdAt DESC")
    List<TaskAttachment> findNonImagesByTaskId(@Param("taskId") Long taskId);
    
    long countByTask(Task task);
    
    long countByTaskId(Long taskId);
    
    long countByTaskIdAndContentTypeStartingWith(Long taskId, String contentTypePrefix);
}
