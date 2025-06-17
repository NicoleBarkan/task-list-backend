package com.tasklist.taskapi.service;

import com.tasklist.taskapi.model.Task;
import com.tasklist.taskapi.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getTasks() {
        return taskRepository.findAll();
    }

    public Task addTask(Task task) {
        if (task.getCreatedOn() == null) {
            task.setCreatedOn(LocalDateTime.now());
        }
        return taskRepository.save(task);
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    public Optional<Task> updateTask(Long id, Task updatedTask) {
        return taskRepository.findById(id).map(existingTask -> {
            existingTask.setTitle(updatedTask.getTitle());
            existingTask.setDescription(updatedTask.getDescription());
            existingTask.setType(updatedTask.getType());
            existingTask.setStatus(updatedTask.getStatus());
            return taskRepository.save(existingTask);
        });
    }
}
