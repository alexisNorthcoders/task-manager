package com.projects.taskmanager.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.projects.taskmanager.model.Task;
import com.projects.taskmanager.model.TaskStatus;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByStatus(TaskStatus status);

    // Basic pagination with optional filtering
    @Query("""
        SELECT t FROM Task t
        WHERE (:completed IS NULL OR t.completed = :completed)
        AND (:titleContains IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :titleContains, '%')))
        """)
    Page<Task> findTasksWithFilters(
        @Param("completed") Boolean completed,
        @Param("titleContains") String titleContains,
        Pageable pageable
    );

    // Advanced filtering with multiple criteria
    @Query("""
        SELECT t FROM Task t
        WHERE (:completed IS NULL OR t.completed = :completed)
        AND (:titleContains IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :titleContains, '%')))
        AND (:status IS NULL OR t.status = :status)
        AND (:dueDateFrom IS NULL OR t.dueDate >= :dueDateFrom)
        AND (:dueDateTo IS NULL OR t.dueDate <= :dueDateTo)
        AND (:estimationHoursMin IS NULL OR t.estimationHours >= :estimationHoursMin)
        AND (:estimationHoursMax IS NULL OR t.estimationHours <= :estimationHoursMax)
        """)
    Page<Task> findTasksWithAdvancedFilters(
        @Param("completed") Boolean completed,
        @Param("titleContains") String titleContains,
        @Param("status") TaskStatus status,
        @Param("dueDateFrom") LocalDate dueDateFrom,
        @Param("dueDateTo") LocalDate dueDateTo,
        @Param("estimationHoursMin") Integer estimationHoursMin,
        @Param("estimationHoursMax") Integer estimationHoursMax,
        Pageable pageable
    );

    // Count tasks for statistics
    @Query("SELECT COUNT(t) FROM Task t WHERE (:completed IS NULL OR t.completed = :completed)")
    long countByCompleted(@Param("completed") Boolean completed);
}
