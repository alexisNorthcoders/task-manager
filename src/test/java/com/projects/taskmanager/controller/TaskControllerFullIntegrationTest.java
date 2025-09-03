package com.projects.taskmanager.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.projects.taskmanager.model.Task;
import com.projects.taskmanager.model.TaskStatus;
import com.projects.taskmanager.repository.TaskRepository;

@SpringBootTest
@AutoConfigureGraphQlTester
@ActiveProfiles("test")
@Transactional
@DisplayName("TaskController GraphQL API")
class TaskControllerFullIntegrationTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        
        // Seed test data
        Task task1 = Task.create("Integration Test Task 1", "Description 1");
        task1.setDueDate(LocalDate.now().plusDays(5));
        task1.setEstimationHours(3);
        taskRepository.save(task1);

        Task task2 = Task.create("Integration Test Task 2", "Description 2");
        task2.setCompleted(true);
        taskRepository.save(task2);

        Task task3 = Task.create("In Progress Task", "Working on it");
        task3.setStatus(TaskStatus.IN_PROGRESS);
        taskRepository.save(task3);
    }

    @Nested
    @DisplayName("when performing end-to-end operations")
    class EndToEndOperations {

        @Test
        @DisplayName("should persist and return task when creating and querying")
        void shouldPersistAndReturnTaskWhenCreatingAndQuerying() {
        // When - Create task
        String createResponse = graphQlTester
            .document("""
                mutation {
                    createTask(input: {
                        title: "E2E Test Task"
                        description: "End-to-end test"
                        completed: false
                        dueDate: "2024-12-25"
                        estimationHours: 8
                    }) {
                        id
                        title
                        description
                        completed
                        status
                        dueDate
                        estimationHours
                    }
                }
                """)
            .execute()
            .path("createTask.id")
            .entity(String.class)
            .get();

        // Then - Query the created task
        graphQlTester
            .document("""
                query($id: ID!) {
                    task(id: $id) {
                        id
                        title
                        description
                        completed
                        status
                        dueDate
                        estimationHours
                    }
                }
                """)
            .variable("id", createResponse)
            .execute()
            .path("task.title").entity(String.class).isEqualTo("E2E Test Task")
            .path("task.description").entity(String.class).isEqualTo("End-to-end test")
            .path("task.completed").entity(Boolean.class).isEqualTo(false)
            .path("task.status").entity(TaskStatus.class).isEqualTo(TaskStatus.TODO)
            .path("task.dueDate").entity(String.class).isEqualTo("2024-12-25")
            .path("task.estimationHours").entity(Integer.class).isEqualTo(8);
        }

        @Test
        @DisplayName("should persist changes when updating and querying task")
        void shouldPersistChangesWhenUpdatingAndQuerying() {
        // Given - Get the first task ID
        String taskId = graphQlTester
            .document("""
                query {
                    tasks {
                        id
                        title
                    }
                }
                """)
            .execute()
            .path("tasks[0].id")
            .entity(String.class)
            .get();

        // When - Update the task
        graphQlTester
            .document("""
                mutation($id: ID!) {
                    updateTask(id: $id, input: {
                        title: "Updated E2E Task"
                        completed: true
                        estimationHours: 10
                    }) {
                        id
                        title
                        completed
                        status
                        estimationHours
                    }
                }
                """)
            .variable("id", taskId)
            .execute()
            .path("updateTask.title").entity(String.class).isEqualTo("Updated E2E Task")
            .path("updateTask.completed").entity(Boolean.class).isEqualTo(true)
            .path("updateTask.status").entity(TaskStatus.class).isEqualTo(TaskStatus.DONE)
            .path("updateTask.estimationHours").entity(Integer.class).isEqualTo(10);

        // Then - Verify persistence by querying again
        graphQlTester
            .document("""
                query($id: ID!) {
                    task(id: $id) {
                        title
                        completed
                        status
                        estimationHours
                    }
                }
                """)
            .variable("id", taskId)
            .execute()
            .path("task.title").entity(String.class).isEqualTo("Updated E2E Task")
            .path("task.completed").entity(Boolean.class).isEqualTo(true)
            .path("task.status").entity(TaskStatus.class).isEqualTo(TaskStatus.DONE)
            .path("task.estimationHours").entity(Integer.class).isEqualTo(10);
        }

        @Test
        @DisplayName("should remove task from database when deleting")
        void shouldRemoveTaskFromDatabaseWhenDeleting() {

    @Nested
    @DisplayName("when querying tasks by status")
    class QueryingTasksByStatus {

        @Test
        @DisplayName("should return correct tasks for each status")
        void shouldReturnCorrectTasksForEachStatus() {
        // When & Then - Query TODO tasks
        graphQlTester
            .document("""
                query {
                    tasksByStatus(status: TODO) {
                        id
                        title
                        status
                        completed
                    }
                }
                """)
            .execute()
            .path("tasksByStatus")
            .entityList(Object.class)
            .hasSize(1) // Should have 1 TODO task from setup
            .path("tasksByStatus[0].status")
            .entity(TaskStatus.class)
            .isEqualTo(TaskStatus.TODO);

        // Query DONE tasks
        graphQlTester
            .document("""
                query {
                    tasksByStatus(status: DONE) {
                        id
                        title
                        status
                        completed
                    }
                }
                """)
            .execute()
            .path("tasksByStatus")
            .entityList(Object.class)
            .hasSize(1) // Should have 1 DONE task from setup
            .path("tasksByStatus[0].status")
            .entity(TaskStatus.class)
            .isEqualTo(TaskStatus.DONE);

        // Query IN_PROGRESS tasks
        graphQlTester
            .document("""
                query {
                    tasksByStatus(status: IN_PROGRESS) {
                        id
                        title
                        status
                        completed
                    }
                }
                """)
            .execute()
            .path("tasksByStatus")
            .entityList(Object.class)
            .hasSize(1) // Should have 1 IN_PROGRESS task from setup
            .path("tasksByStatus[0].status")
            .entity(TaskStatus.class)
            .isEqualTo(TaskStatus.IN_PROGRESS);
        }
    }


        // Given - Get task count before deletion
        int initialCount = graphQlTester
            .document("""
                query {
                    tasks {
                        id
                    }
                }
                """)
            .execute()
            .path("tasks")
            .entityList(Object.class)
            .get()
            .size();

        // Get the first task ID for deletion
        String taskId = graphQlTester
            .document("""
                query {
                    tasks {
                        id
                    }
                }
                """)
            .execute()
            .path("tasks[0].id")
            .entity(String.class)
            .get();

        // When - Delete the task
        graphQlTester
            .document("""
                mutation($id: ID!) {
                    deleteTask(id: $id)
                }
                """)
            .variable("id", taskId)
            .execute()
            .path("deleteTask")
            .entity(Boolean.class)
            .isEqualTo(true);

        // Then - Verify task is deleted
        int finalCount = graphQlTester
            .document("""
                query {
                    tasks {
                        id
                    }
                }
                """)
            .execute()
            .path("tasks")
            .entityList(Object.class)
            .get()
            .size();

        assertEquals(initialCount - 1, finalCount);

        // Verify the specific task is gone
        graphQlTester
            .document("""
                query($id: ID!) {
                    task(id: $id) {
                        id
                    }
                }
                """)
            .variable("id", taskId)
            .execute()
            .path("task")
            .valueIsNull();
        }
    }

    @Nested
    @DisplayName("when validating input data")
    class ValidatingInputData {

        @Test
        @DisplayName("should return errors when creating task with invalid data")
        void shouldReturnErrorsWhenCreatingTaskWithInvalidData() {
        // Test missing required title
        graphQlTester
            .document("""
                mutation {
                    createTask(input: {
                        description: "No title provided"
                        estimationHours: -5
                    }) {
                        id
                        title
                    }
                }
                """)
            .execute()
            .errors()
            .satisfy(errors -> {
                assertFalse(errors.isEmpty());
                // Should contain validation errors
            });
    }

        @Test
        @DisplayName("should return errors when updating task with invalid data")
        void shouldReturnErrorsWhenUpdatingTaskWithInvalidData() {
        // Given - Get a task ID
        String taskId = graphQlTester
            .document("""
                query {
                    tasks {
                        id
                    }
                }
                """)
            .execute()
            .path("tasks[0].id")
            .entity(String.class)
            .get();

        // When & Then - Try to update with invalid data
        graphQlTester
            .document("""
                mutation($id: ID!) {
                    updateTask(id: $id, input: {
                        title: ""
                        estimationHours: -10
                    }) {
                        id
                        title
                    }
                }
                """)
            .variable("id", taskId)
            .execute()
            .errors()
            .satisfy(errors -> {
                assertFalse(errors.isEmpty());
                // Should contain validation errors
            });
        }
    }
}
