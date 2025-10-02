package com.tasklist.taskapi.controller;

import com.tasklist.taskapi.dto.TaskDto;
import com.tasklist.taskapi.model.Task;
import com.tasklist.taskapi.service.TaskService;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/tasks", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("isAuthenticated()")
public class TaskController {

    private final TaskService taskService;
    public TaskController(TaskService taskService) { this.taskService = taskService; }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    @GetMapping
    public ResponseEntity<List<TaskDto>> getAll(@RequestParam(required = false) Long groupId) {
        List<TaskDto> list = (groupId == null) ? taskService.list() : taskService.listByGroup(groupId);
        return ResponseEntity.ok(list);
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getById(@PathVariable Long id) {
        return taskService.getTaskById(id)
                .map(TaskDto::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskDto> create(@RequestBody Task task) {
        var saved = taskService.addTask(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(TaskDto.from(saved));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskDto> update(@PathVariable Long id, @RequestBody Task updated) {
        updated.setId(id);
        return taskService.updateTask(id, updated)
                .map(TaskDto::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return taskService.getTaskById(id)
                .map(t -> { taskService.deleteTask(id); return ResponseEntity.noContent().<Void>build(); })
                .orElse(ResponseEntity.notFound().build());
    }
}