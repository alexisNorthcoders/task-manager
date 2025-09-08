package com.projects.taskmanager.testutil;

import com.projects.taskmanager.model.Task;
import com.projects.taskmanager.model.TaskStatus;
import com.projects.taskmanager.graphql.input.CreateTaskInput;
import com.projects.taskmanager.graphql.input.UpdateTaskInput;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Factory class for creating test data objects.
 */
public class TestDataFactory {

    public static Task createSimpleTask() {
        return Task.create("Test Task", "Test Description");
    }

    public static Task createTaskWithStatus(TaskStatus status) {
        Task task = Task.create("Task with " + status, "Description for " + status);
        task.setStatus(status);
        return task;
    }

    public static Task createCompletedTask() {
        Task task = Task.create("Completed Task", "This task is done");
        task.setCompleted(true);
        return task;
    }

    public static Task createTaskWithAllFields() {
        Task task = Task.create("Full Task", "Complete task with all fields");
        task.setCompleted(false);
        task.setDueDate(LocalDate.now().plusDays(7));
        task.setEstimationHours(5.0);
        return task;
    }

    public static Task createTaskWithLongTitle() {
        String longTitle = "A".repeat(300); // Exceeds max length
        return Task.create(longTitle, "Description");
    }

    public static Task createTaskWithLongDescription() {
        String longDesc = "B".repeat(1100); // Exceeds max length
        return Task.create("Title", longDesc);
    }

    public static List<Task> createMultipleTasks() {
        return Arrays.asList(
            Task.create("Task 1", "First task"),
            Task.create("Task 2", "Second task"),
            createCompletedTask()
        );
    }

    public static CreateTaskInput createValidCreateInput() {
        CreateTaskInput input = new CreateTaskInput();
        input.setTitle("New Task");
        input.setDescription("New task description");
        input.setCompleted(false);
        input.setDueDate("2024-12-31");
        input.setEstimationHours(3.0);
        return input;
    }

    public static CreateTaskInput createInvalidCreateInput() {
        CreateTaskInput input = new CreateTaskInput();
        // Missing required title
        input.setDescription("Description without title");
        input.setEstimationHours(-1.0); // Invalid estimation
        return input;
    }

    public static UpdateTaskInput createValidUpdateInput() {
        UpdateTaskInput input = new UpdateTaskInput();
        input.setTitle("Updated Title");
        input.setCompleted(true);
        return input;
    }

    public static UpdateTaskInput createPartialUpdateInput() {
        UpdateTaskInput input = new UpdateTaskInput();
        input.setCompleted(true); // Only update completion status
        return input;
    }

    public static UpdateTaskInput createInvalidUpdateInput() {
        UpdateTaskInput input = new UpdateTaskInput();
        input.setTitle(""); // Empty title
        input.setEstimationHours(-5.0); // Invalid estimation
        return input;
    }
}
