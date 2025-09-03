package com.projects.taskmanager.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.projects.taskmanager.config.TaskProperties;
import com.projects.taskmanager.model.Task;
import com.projects.taskmanager.model.TaskStatus;
import com.projects.taskmanager.repository.TaskRepository;
import com.projects.taskmanager.service.exception.TaskNotFoundException;
import com.projects.taskmanager.testutil.TestDataFactory;
import com.projects.taskmanager.util.TextNormalizer;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskProperties taskProperties;

    @Mock
    private TextNormalizer textNormalizer;

    @InjectMocks
    private TaskService taskService;

    private Task testTask;
    private List<Task> testTasks;

    @BeforeEach
    void setUp() {
        testTask = TestDataFactory.createSimpleTask();
        testTasks = TestDataFactory.createMultipleTasks();
        
        // Default property values - made lenient to avoid unnecessary stubbing errors
        lenient().when(taskProperties.getTitleMaxLength()).thenReturn(255);
        lenient().when(taskProperties.getDescriptionMaxLength()).thenReturn(1000);
        
        // Default text normalizer behavior - made lenient
        lenient().when(textNormalizer.normalizeTitle(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Nested
    @DisplayName("when retrieving tasks")
    class RetrievingTasks {

        @Test
        @DisplayName("should return all tasks when getAllTasks is called")
        void shouldReturnAllTasks() {
            // Given
            when(taskRepository.findAll()).thenReturn(testTasks);

            // When
            List<Task> result = taskService.getAllTasks();

            // Then
            assertEquals(testTasks.size(), result.size());
            assertEquals(testTasks, result);
            verify(taskRepository).findAll();
        }

        @Test
        @DisplayName("should return tasks with given status when getAllTasksByStatus is called")
        void shouldReturnTasksWithGivenStatus() {
            // Given
            TaskStatus status = TaskStatus.TODO;
            List<Task> todoTasks = List.of(TestDataFactory.createTaskWithStatus(TaskStatus.TODO));
            when(taskRepository.findByStatus(status)).thenReturn(todoTasks);

            // When
            List<Task> result = taskService.getAllTasksByStatus(status);

            // Then
            assertEquals(todoTasks.size(), result.size());
            assertEquals(todoTasks, result);
            verify(taskRepository).findByStatus(status);
        }

        @Test
        @DisplayName("should return task when getTaskById is called with existing ID")
        void shouldReturnTaskWhenExists() {
            // Given
            Long taskId = 1L;
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));

            // When
            Optional<Task> result = taskService.getTaskById(taskId);

            // Then
            assertTrue(result.isPresent());
            assertEquals(testTask, result.get());
            verify(taskRepository).findById(taskId);
        }

        @Test
        @DisplayName("should return empty when getTaskById is called with non-existing ID")
        void shouldReturnEmptyWhenTaskDoesNotExist() {
            // Given
            Long taskId = 999L;
            when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

            // When
            Optional<Task> result = taskService.getTaskById(taskId);

            // Then
            assertFalse(result.isPresent());
            verify(taskRepository).findById(taskId);
        }
    }

    @Nested
    @DisplayName("when creating tasks")
    class CreatingTasks {

        @Test
        @DisplayName("should save and return task when createTask is called with valid data")
        void shouldCreateTaskWithValidData() {
            // Given
            Task taskToCreate = TestDataFactory.createSimpleTask();
            Task savedTask = TestDataFactory.createSimpleTask();
            when(taskRepository.save(taskToCreate)).thenReturn(savedTask);

            // When
            Task result = taskService.createTask(taskToCreate);

            // Then
            assertEquals(savedTask, result);
            verify(textNormalizer).normalizeTitle(taskToCreate.getTitle());
            verify(taskRepository).save(taskToCreate);
        }

        @Test
        @DisplayName("should throw exception when createTask is called with title too long")
        void shouldThrowExceptionWhenTitleTooLong() {
            // Given
            Task taskWithLongTitle = TestDataFactory.createTaskWithLongTitle();
            when(taskProperties.getTitleMaxLength()).thenReturn(255);

            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.createTask(taskWithLongTitle)
            );
            
            assertTrue(exception.getMessage().contains("Title length exceeds max"));
            verify(taskRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when createTask is called with description too long")
        void shouldThrowExceptionWhenDescriptionTooLong() {
            // Given
            Task taskWithLongDesc = TestDataFactory.createTaskWithLongDescription();
            when(taskProperties.getDescriptionMaxLength()).thenReturn(1000);

            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.createTask(taskWithLongDesc)
            );
            
            assertTrue(exception.getMessage().contains("Description length exceeds max"));
            verify(taskRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("when deleting tasks")
    class DeletingTasks {

        @Test
        @DisplayName("should return true when deleteTask is called with existing ID")
        void shouldReturnTrueWhenTaskExists() {
            // Given
            Long taskId = 1L;
            when(taskRepository.existsById(taskId)).thenReturn(true);

            // When
            boolean result = taskService.deleteTask(taskId);

            // Then
            assertTrue(result);
            verify(taskRepository).existsById(taskId);
            verify(taskRepository).deleteById(taskId);
        }

        @Test
        @DisplayName("should return false when deleteTask is called with non-existing ID")
        void shouldReturnFalseWhenTaskDoesNotExist() {
            // Given
            Long taskId = 999L;
            when(taskRepository.existsById(taskId)).thenReturn(false);

            // When
            boolean result = taskService.deleteTask(taskId);

            // Then
            assertFalse(result);
            verify(taskRepository).existsById(taskId);
            verify(taskRepository, never()).deleteById(taskId);
        }
    }

    @Nested
    @DisplayName("when updating tasks")
    class UpdatingTasks {

        @Test
        @DisplayName("should update and return task when updateTask is called with valid data")
        void shouldUpdateTaskWithValidData() {
            // Given
            Long taskId = 1L;
            String newTitle = "Updated Title";
            String newDescription = "Updated Description";
            Boolean newCompleted = true;
            
            Task existingTask = TestDataFactory.createSimpleTask();
            Task updatedTask = TestDataFactory.createSimpleTask();
            updatedTask.setTitle(newTitle);
            updatedTask.setDescription(newDescription);
            updatedTask.setCompleted(newCompleted);
            
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
            when(taskRepository.save(existingTask)).thenReturn(updatedTask);

            // When
            taskService.updateTask(taskId, newTitle, newDescription, newCompleted, null, null);

            // Then
            verify(taskRepository).findById(taskId);
            verify(textNormalizer).normalizeTitle(newTitle);
            verify(taskRepository).save(existingTask);
            assertEquals(newTitle, existingTask.getTitle());
            assertEquals(newDescription, existingTask.getDescription());
            assertEquals(newCompleted, existingTask.isCompleted());
        }

        @Test
        @DisplayName("should only update provided fields when updateTask is called with partial data")
        void shouldOnlyUpdateProvidedFields() {
            // Given
            Long taskId = 1L;
            String originalTitle = "Original Title";
            String originalDescription = "Original Description";
            
            Task existingTask = TestDataFactory.createSimpleTask();
            existingTask.setTitle(originalTitle);
            existingTask.setDescription(originalDescription);
            existingTask.setCompleted(false);
            
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
            when(taskRepository.save(existingTask)).thenReturn(existingTask);

            // When - only update completion status
            taskService.updateTask(taskId, null, null, true, null, null);

            // Then
            assertEquals(originalTitle, existingTask.getTitle()); // Should remain unchanged
            assertEquals(originalDescription, existingTask.getDescription()); // Should remain unchanged
            assertTrue(existingTask.isCompleted()); // Should be updated
            verify(textNormalizer, never()).normalizeTitle(any()); // Should not normalize since title wasn't updated
        }

        @Test
        @DisplayName("should throw exception when updateTask is called with non-existing ID")
        void shouldThrowExceptionWhenTaskNotFound() {
            // Given
            Long taskId = 999L;
            when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(
                TaskNotFoundException.class,
                () -> taskService.updateTask(taskId, "New Title", null, null, null, null)
            );
            
            verify(taskRepository).findById(taskId);
            verify(taskRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when updateTask is called with invalid due date")
        void shouldThrowExceptionWhenInvalidDueDate() {
            // Given
            Long taskId = 1L;
            Task existingTask = TestDataFactory.createSimpleTask();
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.updateTask(taskId, null, null, null, "invalid-date", null)
            );
            
            assertTrue(exception.getMessage().contains("Invalid dueDate format"));
        }

        @Test
        @DisplayName("should update due date when updateTask is called with valid date")
        void shouldUpdateDueDateWhenValid() {
            // Given
            Long taskId = 1L;
            String validDate = "2024-12-31";
            Task existingTask = TestDataFactory.createSimpleTask();
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
            when(taskRepository.save(existingTask)).thenReturn(existingTask);

            // When
            taskService.updateTask(taskId, null, null, null, validDate, null);

            // Then
            assertEquals(LocalDate.parse(validDate), existingTask.getDueDate());
        }

        @Test
        @DisplayName("should throw exception when updateTask is called with invalid estimation hours")
        void shouldThrowExceptionWhenInvalidEstimationHours() {
            // Given
            Long taskId = 1L;
            Task existingTask = TestDataFactory.createSimpleTask();
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

            // When & Then (negative hours)
            IllegalArgumentException exception1 = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.updateTask(taskId, null, null, null, null, -1)
            );
            assertTrue(exception1.getMessage().contains("must be >= 0"));

            // When & Then (unreasonably large hours)
            IllegalArgumentException exception2 = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.updateTask(taskId, null, null, null, null, 20000)
            );
            assertTrue(exception2.getMessage().contains("unreasonably large"));
        }
    }
}