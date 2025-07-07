package com.tasklist.taskapi.service;

import com.tasklist.taskapi.model.Task;

import java.util.List;
import java.util.Optional;

public interface TaskService {
    List<Task> getTasks();
    Task addTask(Task task);
    Task updateTask(Task task);
    Optional<Task> updateTask(Long id, Task updatedTask);
    void deleteTask(Long id);
    Optional<Task> getTaskById(Long id);
}