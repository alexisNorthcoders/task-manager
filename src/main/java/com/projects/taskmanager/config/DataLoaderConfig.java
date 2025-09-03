package com.projects.taskmanager.config;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.dataloader.BatchLoader;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderFactory;
import org.dataloader.DataLoaderOptions;
import org.dataloader.DataLoaderRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.projects.taskmanager.model.User;
import com.projects.taskmanager.repository.UserRepository;

@Configuration
public class DataLoaderConfig {

    private final UserRepository userRepository;

    public DataLoaderConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public DataLoaderRegistry dataLoaderRegistry() {
        DataLoaderRegistry registry = new DataLoaderRegistry();

        // DataLoader for batch loading users by IDs
        registry.register("userDataLoader", createUserDataLoader());

        // DataLoader for batch loading users by task IDs
        registry.register("usersByTaskDataLoader", createUsersByTaskDataLoader());

        return registry;
    }

    private DataLoader<Long, User> createUserDataLoader() {
        BatchLoader<Long, User> userBatchLoader = userIds -> CompletableFuture.supplyAsync(() -> {
            List<User> users = userRepository.findAllById(userIds);
            Map<Long, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, Function.identity()));

            // Return users in the same order as requested IDs, null for not found
            return userIds.stream()
                    .map(userMap::get)
                    .toList();
        });

        return DataLoaderFactory.newDataLoader(userBatchLoader, DataLoaderOptions.newOptions());
    }

    private DataLoader<Long, List<User>> createUsersByTaskDataLoader() {
        BatchLoader<Long, List<User>> usersByTaskBatchLoader = taskIds -> CompletableFuture.supplyAsync(() -> {
            // Find all users assigned to the given task IDs
            List<User> users = userRepository.findUsersByAssignedTaskIds(taskIds);

            // Group users by task ID
            Map<Long, List<User>> usersByTaskMap = users.stream()
                    .flatMap(user -> user.getAssignedTasks().stream()
                            .filter(task -> taskIds.contains(task.getId()))
                            .map(task -> Map.entry(task.getId(), user)))
                    .collect(Collectors.groupingBy(
                            Map.Entry::getKey,
                            Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                    ));

            // Return users for each task ID, empty list for tasks with no assigned users
            return taskIds.stream()
                    .map(taskId -> usersByTaskMap.getOrDefault(taskId, List.of()))
                    .toList();
        });

        return DataLoaderFactory.newDataLoader(usersByTaskBatchLoader, DataLoaderOptions.newOptions());
    }
}
