package com.tasklist.taskapi.repository;

import com.tasklist.taskapi.model.Task;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByGroupId(Long groupId);
    Optional<Task> findByIdAndGroupId(Long id, Long groupId);
    boolean existsByGroupId(Long groupId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update Task t set t.group.id = :groupId where t.assignedTo.id = :userId")
    int updateGroupForAssignedUserTasks(@Param("userId") Long userId, @Param("groupId") Long groupId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update Task t set t.assignedTo = null, t.group.id = :groupId where t.assignedTo.id = :userId")
    int unassignTasksForUser(@Param("userId") Long userId, @Param("groupId") Long groupId);
}
