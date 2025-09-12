package com.projects.taskmanager.repository;

import com.projects.taskmanager.model.TaskActivity;
import com.projects.taskmanager.model.Task;
import com.projects.taskmanager.model.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskActivityRepository extends JpaRepository<TaskActivity, Long> {
    
    List<TaskActivity> findByTaskOrderByCreatedAtDesc(Task task);
    
    List<TaskActivity> findByTaskIdOrderByCreatedAtDesc(Long taskId);
    
    List<TaskActivity> findByTaskAndActivityTypeOrderByCreatedAtDesc(Task task, ActivityType activityType);
    
    List<TaskActivity> findByTaskIdAndActivityTypeOrderByCreatedAtDesc(Long taskId, ActivityType activityType);
    
    @Query("SELECT a FROM TaskActivity a WHERE a.task.id = :taskId ORDER BY a.createdAt DESC")
    List<TaskActivity> findRecentActivitiesByTaskId(@Param("taskId") Long taskId);
    
    long countByTask(Task task);
    
    long countByTaskId(Long taskId);
}
