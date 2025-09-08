package com.projects.taskmanager.service;

import com.projects.taskmanager.model.Task;
import com.projects.taskmanager.model.User;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Notify all users about a task creation
     */
    public void notifyTaskCreated(Task task) {
        Map<String, Object> notification = createTaskNotification("TASK_CREATED", task);
        messagingTemplate.convertAndSend("/topic/tasks", notification);
    }

    /**
     * Notify all users about a task update
     */
    public void notifyTaskUpdated(Task task) {
        Map<String, Object> notification = createTaskNotification("TASK_UPDATED", task);
        messagingTemplate.convertAndSend("/topic/tasks", notification);
    }

    /**
     * Notify all users about a task deletion
     */
    public void notifyTaskDeleted(Long taskId) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "TASK_DELETED");
        notification.put("taskId", taskId);
        notification.put("timestamp", System.currentTimeMillis());
        
        messagingTemplate.convertAndSend("/topic/tasks", notification);
    }

    /**
     * Notify all users about bulk operations
     */
    public void notifyBulkOperation(String operationType, int count) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "BULK_OPERATION");
        notification.put("operation", operationType);
        notification.put("count", count);
        notification.put("timestamp", System.currentTimeMillis());
        
        messagingTemplate.convertAndSend("/topic/tasks", notification);
    }

    /**
     * Notify assigned users about task changes
     */
    public void notifyAssignedUsers(Task task, String eventType) {
        Map<String, Object> notification = createTaskNotification(eventType, task);
        
        // Send notification to each assigned user
        for (User user : task.getAssignedUsers()) {
            messagingTemplate.convertAndSendToUser(
                user.getUsername(), 
                "/queue/notifications", 
                notification
            );
        }
    }

    /**
     * Send user presence update
     */
    public void notifyUserPresence(String username, boolean isOnline) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "USER_PRESENCE");
        notification.put("username", username);
        notification.put("isOnline", isOnline);
        notification.put("timestamp", System.currentTimeMillis());
        
        messagingTemplate.convertAndSend("/topic/presence", notification);
    }

    private Map<String, Object> createTaskNotification(String type, Task task) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", type);
        notification.put("task", convertTaskToMap(task));
        notification.put("timestamp", System.currentTimeMillis());
        return notification;
    }

    private Map<String, Object> convertTaskToMap(Task task) {
        Map<String, Object> taskMap = new HashMap<>();
        taskMap.put("id", task.getId());
        taskMap.put("title", task.getTitle());
        taskMap.put("description", task.getDescription());
        taskMap.put("completed", task.isCompleted());
        taskMap.put("status", task.getStatus().toString());
        taskMap.put("dueDate", task.getDueDate() != null ? task.getDueDate().toString() : null);
        taskMap.put("estimationHours", task.getEstimationHours());
        taskMap.put("createdAt", task.getCreatedAt() != null ? task.getCreatedAt().toString() : null);
        taskMap.put("updatedAt", task.getUpdatedAt() != null ? task.getUpdatedAt().toString() : null);
        
        // Add assigned user IDs
        taskMap.put("assignedUserIds", task.getAssignedUsers().stream()
                .map(User::getId)
                .toList());
        
        return taskMap;
    }
}