package com.projects.taskmanager.model;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

class TaskTest {

    private static void setId(Task task, Long id) {
        try {
            Field field = Task.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(task, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void equalsAndHashCode_useIdWhenPresent() {
        Task a = new Task("Title", "Desc", false);
        Task b = new Task("Other", "Other", true);
        setId(a, 1L);
        setId(b, 1L);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());

        Task c = new Task("Different", null, false);
        setId(c, 2L);
        assertNotEquals(a, c);
    }

    @Test
    void equals_whenNoId_thenOnlySameInstanceIsEqual() {
        Task a = new Task("T", null, false);
        Task b = new Task("T", null, false);
        assertNotEquals(a, b);
        assertEquals(a, a);
    }

    @Test
    void toString_containsKeyFields() {
        Task a = Task.create("Read", "Docs");
        String s = a.toString();
        assertTrue(s.contains("Task{"));
        assertTrue(s.contains("title='Read'"));
        assertTrue(s.contains("completed=false"));
    }

    @Test
    void status_syncsWithCompleted_whenSettingCompleted() {
        Task a = Task.create("Read", "Docs");
        assertEquals(TaskStatus.TODO, a.getStatus());
        assertFalse(a.isCompleted());

        a.setCompleted(true);
        assertEquals(TaskStatus.DONE, a.getStatus());
        assertTrue(a.isCompleted());

        a.setCompleted(false);
        assertEquals(TaskStatus.TODO, a.getStatus());
        assertFalse(a.isCompleted());
    }

    @Test
    void status_syncsWithCompleted_whenSettingStatus() {
        Task a = Task.create("Read", "Docs");
        a.setStatus(TaskStatus.IN_PROGRESS);
        assertFalse(a.isCompleted());
        assertEquals(TaskStatus.IN_PROGRESS, a.getStatus());

        a.setStatus(TaskStatus.DONE);
        assertTrue(a.isCompleted());
        assertEquals(TaskStatus.DONE, a.getStatus());

        a.setStatus(TaskStatus.TODO);
        assertFalse(a.isCompleted());
        assertEquals(TaskStatus.TODO, a.getStatus());
    }
}


