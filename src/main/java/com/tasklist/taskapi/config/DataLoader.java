package com.tasklist.taskapi.config;

import com.tasklist.taskapi.model.Task;
import com.tasklist.taskapi.service.TaskService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class DataLoader {

    @Bean
    public CommandLineRunner loadData(TaskService taskService) {
        return args -> {
            if (taskService.getTasks().isEmpty()) {
                taskService.addTask(new Task("Buy groceries", "Milk, eggs, bread", "personal", "pending", LocalDateTime.now()));
                taskService.addTask(new Task("Finish project", "Backend API and Angular UI", "work", "in progress", LocalDateTime.now()));
                taskService.addTask(new Task("Call dentist", "Schedule appointment", "personal", "done", LocalDateTime.now()));
            }
        };
    }
}
