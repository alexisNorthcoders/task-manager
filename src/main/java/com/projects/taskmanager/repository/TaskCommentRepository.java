package com.projects.taskmanager.repository;

import com.projects.taskmanager.model.TaskComment;
import com.projects.taskmanager.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {
    
    List<TaskComment> findByTaskOrderByCreatedAtAsc(Task task);
    
    List<TaskComment> findByTaskIdOrderByCreatedAtAsc(Long taskId);
    
    List<TaskComment> findByParentCommentIsNullAndTaskOrderByCreatedAtAsc(Task task);
    
    List<TaskComment> findByParentCommentIdOrderByCreatedAtAsc(Long parentCommentId);
    
    @Query("SELECT c FROM TaskComment c WHERE c.task.id = :taskId AND c.parentComment IS NULL ORDER BY c.createdAt ASC")
    List<TaskComment> findTopLevelCommentsByTaskId(@Param("taskId") Long taskId);
    
    long countByTask(Task task);
    
    long countByTaskId(Long taskId);
}
