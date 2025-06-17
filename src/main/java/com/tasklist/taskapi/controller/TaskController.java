package com.tasklist.taskapi.controller;

import com.tasklist.taskapi.model.Task;
import com.tasklist.taskapi.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping
    public List<Task> getTasks() {
        return taskService.getTasks();
    }

    @PostMapping
    public Task createTask(@RequestBody Task task) {
        System.out.println("Received: " + task);
        return taskService.addTask(task);
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
