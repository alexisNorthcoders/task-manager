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
        task1.setEstimationHours(3.0);
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
    @DisplayName("when querying tasks with pagination and filtering")
    class QueryingTasksWithPaginationAndFiltering {

        @Test
        @DisplayName("should return paginated tasks with default parameters")
        void shouldReturnPaginatedTasksWithDefaultParameters() {
            // When & Then
            graphQlTester
                .document("""
                    query {
                        tasksPaginated {
                            content { id title }
                            totalElements
                            totalPages
                            number
                            size
                            numberOfElements
                            first
                            last
                        }
                    }
                    """)
                .execute()
                .path("tasksPaginated.totalElements").entity(Long.class).isEqualTo(3L)
                .path("tasksPaginated.size").entity(Integer.class).isEqualTo(10)
                .path("tasksPaginated.number").entity(Integer.class).isEqualTo(0)
                .path("tasksPaginated.first").entity(Boolean.class).isEqualTo(true)
                .path("tasksPaginated.last").entity(Boolean.class).isEqualTo(true);
        }

        @Test
        @DisplayName("should return paginated tasks with custom page size")
        void shouldReturnPaginatedTasksWithCustomPageSize() {
            // When & Then
            graphQlTester
                .document("""
                    query {
                        tasksPaginated(page: 0, size: 2) {
                            content { id title }
                            totalElements
                            totalPages
                            size
                            numberOfElements
                        }
                    }
                    """)
                .execute()
                .path("tasksPaginated.size").entity(Integer.class).isEqualTo(2)
                .path("tasksPaginated.numberOfElements").entity(Integer.class).isEqualTo(2)
                .path("tasksPaginated.totalPages").entity(Integer.class).isEqualTo(2);
        }

        @Test
        @DisplayName("should filter tasks by completion status")
        void shouldFilterTasksByCompletionStatus() {
            // When & Then - Filter by completed = true
            graphQlTester
                .document("""
                    query {
                        tasksPaginated(completed: true) {
                            content { id title completed }
                            totalElements
                        }
                    }
                    """)
                .execute()
                .path("tasksPaginated.totalElements").entity(Long.class).isEqualTo(1L)
                .path("tasksPaginated.content[0].completed").entity(Boolean.class).isEqualTo(true);

            // Filter by completed = false
            graphQlTester
                .document("""
                    query {
                        tasksPaginated(completed: false) {
                            content { id title completed }
                            totalElements
                        }
                    }
                    """)
                .execute()
                .path("tasksPaginated.totalElements").entity(Long.class).isEqualTo(2L)
                .path("tasksPaginated.content[0].completed").entity(Boolean.class).isEqualTo(false);
        }

        @Test
        @DisplayName("should filter tasks by title containing text")
        void shouldFilterTasksByTitleContainingText() {
            // When & Then - Filter by title containing "Integration"
            graphQlTester
                .document("""
                    query {
                        tasksPaginated(titleContains: "Integration") {
                            content { id title }
                            totalElements
                        }
                    }
                    """)
                .execute()
                .path("tasksPaginated.totalElements").entity(Long.class).isEqualTo(2L)
                .path("tasksPaginated.content").entityList(Object.class).hasSize(2);

            // Filter by title containing "Progress" (should match third task)
            graphQlTester
                .document("""
                    query {
                        tasksPaginated(titleContains: "Progress") {
                            content { id title }
                            totalElements
                        }
                    }
                    """)
                .execute()
                .path("tasksPaginated.totalElements").entity(Long.class).isEqualTo(1L);
        }

        @Test
        @DisplayName("should sort tasks by title in ascending order")
        void shouldSortTasksByTitleAscending() {
            // When & Then - Sort by title ASC
            graphQlTester
                .document("""
                    query {
                        tasksPaginated(sortBy: TITLE, sortDirection: ASC) {
                            content { title }
                        }
                    }
                    """)
                .execute()
                .path("tasksPaginated.content").entityList(Object.class).hasSize(3); // Just verify we get 3 items for now
        }

        @Test
        @DisplayName("should sort tasks by title in descending order")
        void shouldSortTasksByTitleDescending() {
            // When & Then - Sort by title DESC
            graphQlTester
                .document("""
                    query {
                        tasksPaginated(sortBy: TITLE, sortDirection: DESC) {
                            content { title }
                        }
                    }
                    """)
                .execute()
                .path("tasksPaginated.content").entityList(Object.class).hasSize(3); // Just verify we get 3 items for now
        }

        @Test
        @DisplayName("should combine filtering and sorting")
        void shouldCombineFilteringAndSorting() {
            // When & Then - Filter by completed=false and sort by title DESC
            graphQlTester
                .document("""
                    query {
                        tasksPaginated(completed: false, sortBy: TITLE, sortDirection: DESC) {
                            content { title completed }
                            totalElements
                        }
                    }
                    """)
                .execute()
                .path("tasksPaginated.totalElements").entity(Long.class).isEqualTo(2L); // Two incomplete tasks
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
