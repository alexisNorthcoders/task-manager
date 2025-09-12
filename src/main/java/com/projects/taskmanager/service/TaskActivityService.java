package com.projects.taskmanager.service;

import com.projects.taskmanager.model.TaskActivity;
import com.projects.taskmanager.model.Task;
import com.projects.taskmanager.model.User;
import com.projects.taskmanager.model.ActivityType;
import com.projects.taskmanager.repository.TaskActivityRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TaskActivityService {
    
    private final TaskActivityRepository taskActivityRepository;
    
    public TaskActivityService(TaskActivityRepository taskActivityRepository) {
        this.taskActivityRepository = taskActivityRepository;
    }
    
    public TaskActivity logActivity(Task task, User user, ActivityType activityType, String description) {
        TaskActivity activity = new TaskActivity(task, user, activityType, description);
        return taskActivityRepository.save(activity);
    }
    
    public TaskActivity logActivity(Task task, User user, ActivityType activityType, String description, String oldValue, String newValue) {
        TaskActivity activity = new TaskActivity(task, user, activityType, description, oldValue, newValue);
        return taskActivityRepository.save(activity);
    }
    
    @Transactional(readOnly = true)
    public List<TaskActivity> getActivitiesByTaskId(Long taskId) {
        return taskActivityRepository.findByTaskIdOrderByCreatedAtDesc(taskId);
    }
    
    @Transactional(readOnly = true)
    public List<TaskActivity> getRecentActivitiesByTaskId(Long taskId) {
        return taskActivityRepository.findRecentActivitiesByTaskId(taskId);
    }
    
    @Transactional(readOnly = true)
    public List<TaskActivity> getActivitiesByTaskAndType(Long taskId, ActivityType activityType) {
        return taskActivityRepository.findByTaskIdAndActivityTypeOrderByCreatedAtDesc(taskId, activityType);
    }
    
    @Transactional(readOnly = true)
    public long getActivityCountByTaskId(Long taskId) {
        return taskActivityRepository.countByTaskId(taskId);
    }
}
