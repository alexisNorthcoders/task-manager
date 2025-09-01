package com.projects.taskmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.projects.taskmanager.model.Task;
import com.projects.taskmanager.model.TaskStatus;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> { 
    List<Task> findByStatus(TaskStatus status);
}
