package com.tasklist.taskapi.repository;

import com.tasklist.taskapi.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
