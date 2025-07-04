package com.tasklist.taskapi.controller;

import com.tasklist.taskapi.model.Task;
import com.tasklist.taskapi.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<Task> getTasks() {
        return taskService.getTasks();
    }

    @PostMapping
    public Task createTask(@RequestBody Task task) {
        System.out.println("Received: " + task);
        return taskService.addTask(task);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task updatedTask) {
        return taskService.getTaskById(id)
            .map(existingTask -> {
                updatedTask.setId(id);
                updatedTask.setCreatedOn(existingTask.getCreatedOn());
                updatedTask.setUpdatedOn(java.time.LocalDateTime.now()); 
                Task savedTask = taskService.updateTask(updatedTask);
                return ResponseEntity.ok(savedTask);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task updatedTask) {
        return taskService.updateTask(id, updatedTask)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<List<Task>> deleteTask(@PathVariable Long id) {
        if (taskService.getTaskById(id).isPresent()) {
            taskService.deleteTask(id);
            List<Task> updatedTasks = taskService.getTasks();
            return ResponseEntity.ok(updatedTasks);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
