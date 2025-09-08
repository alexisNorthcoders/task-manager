package com.projects.taskmanager.service.exception;

public class TaskTemplateNotFoundException extends RuntimeException {
    public TaskTemplateNotFoundException(Long id) {
        super("Task template not found with id: " + id);
    }
}