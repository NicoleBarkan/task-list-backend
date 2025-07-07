package com.tasklist.taskapi.service.impl;

import com.tasklist.taskapi.model.Task;
import com.tasklist.taskapi.repository.TaskRepository;
import com.tasklist.taskapi.service.TaskService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public List<Task> getTasks() {
        return taskRepository.findAll();
    }

    @Override
    public Task addTask(Task task) {
        return taskRepository.save(task);
    }

    @Override
    public Task updateTask(Task task) {
        Optional<Task> existingTaskOpt = taskRepository.findById(task.getId());
        if (existingTaskOpt.isEmpty()) {
            throw new RuntimeException("Task not found with id: " + task.getId());
        }

        Task existingTask = existingTaskOpt.get();

        existingTask.setTitle(task.getTitle());
        existingTask.setDescription(task.getDescription());
        existingTask.setType(task.getType());
        existingTask.setStatus(task.getStatus());
        existingTask.setUpdatedOn(LocalDateTime.now());

        return taskRepository.save(existingTask);
    }

    @Override
    public Optional<Task> updateTask(Long id, Task updatedTask) {
        return taskRepository.findById(id).map(existingTask -> {
            existingTask.setTitle(updatedTask.getTitle());
            existingTask.setDescription(updatedTask.getDescription());
            existingTask.setType(updatedTask.getType());
            existingTask.setStatus(updatedTask.getStatus());
            existingTask.setUpdatedOn(LocalDateTime.now());
            return taskRepository.save(existingTask);
        });
    }

    @Override
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    @Override
    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }
}
