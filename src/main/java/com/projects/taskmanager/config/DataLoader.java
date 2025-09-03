package com.projects.taskmanager.config;

import com.projects.taskmanager.model.Role;
import com.projects.taskmanager.model.User;
import com.projects.taskmanager.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Data loader for development and testing
 */
@Configuration
public class DataLoader {

    @Bean
    @Profile({"local", "dev"})
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Check if users already exist
            if (userRepository.findByUsername("admin") == null) {
                User admin = new User(
                        "admin",
                        "admin@taskmanager.com",
                        "Admin",
                        "User",
                        passwordEncoder.encode("admin123"),
                        Role.ADMIN
                );
                userRepository.save(admin);
                System.out.println("Created admin user - username: admin, password: admin123");
            }

            if (userRepository.findByUsername("user") == null) {
                User user = new User(
                        "user",
                        "user@taskmanager.com",
                        "Test",
                        "User",
                        passwordEncoder.encode("user123"),
                        Role.USER
                );
                userRepository.save(user);
                System.out.println("Created test user - username: user, password: user123");
            }
        };
    }
}
