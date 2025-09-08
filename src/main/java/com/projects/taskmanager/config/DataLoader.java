package com.projects.taskmanager.config;

import com.projects.taskmanager.model.Role;
import com.projects.taskmanager.model.Task;
import com.projects.taskmanager.model.TaskStatus;
import com.projects.taskmanager.model.User;
import com.projects.taskmanager.repository.TaskRepository;
import com.projects.taskmanager.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Set;

/**
 * Data loader for development and testing
 */
@Configuration
public class DataLoader {

    @Bean
    @Profile({"local", "dev"})
    CommandLineRunner initDatabase(UserRepository userRepository, TaskRepository taskRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Check if users already exist
            User admin = userRepository.findByUsername("admin");
            if (admin == null) {
                admin = new User(
                        "admin",
                        "admin@taskmanager.com",
                        "Admin",
                        "User",
                        passwordEncoder.encode("admin123"),
                        Role.ADMIN
                );
                admin = userRepository.save(admin);
                System.out.println("Created admin user - username: admin, password: admin123");
            }

            User user = userRepository.findByUsername("user");
            if (user == null) {
                user = new User(
                        "user",
                        "user@taskmanager.com",
                        "Test",
                        "User",
                        passwordEncoder.encode("user123"),
                        Role.USER
                );
                user = userRepository.save(user);
                System.out.println("Created test user - username: user, password: user123");
            }

            // Create sample tasks if none exist
            if (taskRepository.count() == 0) {
                Task task1 = new Task(
                        "Design new homepage",
                        "Create wireframes and mockups for the new company homepage",
                        false
                );
                task1.setStatus(TaskStatus.IN_PROGRESS);
                task1.setDueDate(LocalDate.now().plusDays(7));
                task1.setEstimationHours(8.5);
                task1.getAssignedUsers().add(admin);
                admin.getAssignedTasks().add(task1);
                taskRepository.save(task1);

                Task task2 = new Task(
                        "Fix authentication bug",
                        "Resolve the login timeout issue reported by users",
                        false
                );
                task2.setStatus(TaskStatus.TODO);
                task2.setDueDate(LocalDate.now().plusDays(3));
                task2.setEstimationHours(4.0);
                task2.getAssignedUsers().add(user);
                user.getAssignedTasks().add(task2);
                taskRepository.save(task2);

                Task task3 = new Task(
                        "Database optimization",
                        "Improve query performance for the reporting module",
                        true
                );
                task3.setStatus(TaskStatus.DONE);
                task3.setDueDate(LocalDate.now().minusDays(2));
                task3.setEstimationHours(12.0);
                task3.getAssignedUsers().addAll(Set.of(admin, user));
                admin.getAssignedTasks().add(task3);
                user.getAssignedTasks().add(task3);
                taskRepository.save(task3);

                Task task4 = new Task(
                        "Write API documentation",
                        "Create comprehensive documentation for the GraphQL API endpoints",
                        false
                );
                task4.setStatus(TaskStatus.TODO);
                task4.setDueDate(LocalDate.now().plusDays(14));
                task4.setEstimationHours(6.5);
                task4.getAssignedUsers().add(admin);
                admin.getAssignedTasks().add(task4);
                taskRepository.save(task4);

                Task task5 = new Task(
                        "Setup monitoring dashboard",
                        "Configure Grafana dashboards for application metrics",
                        false
                );
                task5.setStatus(TaskStatus.IN_PROGRESS);
                task5.setDueDate(LocalDate.now().plusDays(10));
                task5.setEstimationHours(5.5);
                task5.getAssignedUsers().add(user);
                user.getAssignedTasks().add(task5);
                taskRepository.save(task5);

                System.out.println("Created 5 sample tasks with user assignments");
            }
        };
    }
}
