package com.tasklist.taskapi.repository;

import com.tasklist.taskapi.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByGroupId(Long groupId);
    Optional<Task> findByIdAndGroupId(Long id, Long groupId);
    boolean existsByGroupId(Long groupId);
}
