package com.projects.taskmanager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.projects.taskmanager.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by username
    User findByUsername(String username);

    // Find user by email
    User findByEmail(String email);

    // Find users by assigned task IDs (for DataLoader batch loading)
    @Query("""
        SELECT DISTINCT u FROM User u
        JOIN u.assignedTasks t
        WHERE t.id IN :taskIds
        """)
    List<User> findUsersByAssignedTaskIds(@Param("taskIds") List<Long> taskIds);

    // Find users assigned to a specific task
    @Query("""
        SELECT u FROM User u
        JOIN u.assignedTasks t
        WHERE t.id = :taskId
        """)
    List<User> findUsersByTaskId(@Param("taskId") Long taskId);

    // Find tasks count for a user
    @Query("""
        SELECT COUNT(t) FROM Task t
        JOIN t.assignedUsers u
        WHERE u.id = :userId
        """)
    long countTasksByUserId(@Param("userId") Long userId);
}
