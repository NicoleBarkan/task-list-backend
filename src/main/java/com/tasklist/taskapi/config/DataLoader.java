package com.tasklist.taskapi.config;

import com.tasklist.taskapi.service.TaskService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {

    @Bean
    public CommandLineRunner loadData(TaskService taskService) {
        return args -> {};
    }
}
