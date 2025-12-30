package com.tasklist.taskapi.service;

import com.tasklist.taskapi.dto.TaskCreateDto;
import com.tasklist.taskapi.dto.TaskDto;
import com.tasklist.taskapi.dto.TaskUpdateDto;

import java.util.List;
import java.util.Optional;

public interface TaskService {

    List<TaskDto> list();
    List<TaskDto> listByGroup(Long groupId);

    Optional<TaskDto> getTaskById(Long id);

    TaskDto addTask(TaskCreateDto dto);

    Optional<TaskDto> updateTask(Long id, TaskUpdateDto dto);

    boolean deleteTask(Long id);
}
