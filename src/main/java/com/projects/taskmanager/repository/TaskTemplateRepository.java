package com.projects.taskmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.projects.taskmanager.model.TaskTemplate;

import java.util.List;

@Repository
public interface TaskTemplateRepository extends JpaRepository<TaskTemplate, Long> {

    /**
     * Find all templates ordered by name
     */
    List<TaskTemplate> findAllByOrderByNameAsc();

    /**
     * Find templates by name containing text (case insensitive)
     */
    List<TaskTemplate> findByNameContainingIgnoreCase(String name);

    /**
     * Check if template with given name exists
     */
    boolean existsByNameIgnoreCase(String name);
}