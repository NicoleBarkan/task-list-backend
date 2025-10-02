package com.tasklist.taskapi.service.impl;

import com.tasklist.taskapi.dto.TaskDto;
import com.tasklist.taskapi.model.Task;
import com.tasklist.taskapi.model.User;
import com.tasklist.taskapi.repository.TaskRepository;
import com.tasklist.taskapi.repository.UserRepository;
import com.tasklist.taskapi.service.TaskService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import static com.tasklist.taskapi.security.SecurityUtils.hasAnyRole;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskServiceImpl(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        Object principal = auth.getPrincipal();
        String username = null;

        if (principal instanceof User u) {
            return u;
        } else if (principal instanceof UserDetails ud) {
            username = ud.getUsername();
        } else if (principal instanceof String s) {
            username = s;
        }

        if (username == null) return null;
        return userRepository.findByUsername(username).orElse(null);
    }

    private Map<Long, String> usernamesByIds(List<Task> tasks) {
        Set<Long> ids = tasks.stream()
                .map(Task::getAssignedTo)
                .filter(id -> id != null && id > 0) 
                .collect(Collectors.toSet());

        if (ids.isEmpty()) return Map.of();

        return userRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));
    }

    private TaskDto toDto(Task t, Map<Long,String> names) {
        String username = null;
        Long uid = t.getAssignedTo();
        if (uid != null && uid > 0) {
            username = names.get(uid);
        }
        return TaskDto.from(t, username);
    }

    @Override
    public List<Task> getTasks() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
            return List.of();
        }
        if (hasAnyRole(auth, "ADMIN", "MANAGER")) {
            return taskRepository.findAll();
        }

        User current = currentUser();
        Long groupId = current != null && current.getGroup() != null ? current.getGroup().getId() : null;
        if (groupId == null) {
            return List.of();
        }
        return taskRepository.findByGroupId(groupId);
    }

    @Override
    public Task addTask(Task task) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (hasAnyRole(auth, "ADMIN", "MANAGER")) {
            if (task.getGroup() == null || task.getGroup().getId() == null) {
                throw new IllegalArgumentException("Group must be set for a task");
            }
            return taskRepository.save(task);
        }

        User current = currentUser();
        if (current == null || current.getGroup() == null) {
            throw new AccessDeniedException("No group assigned");
        }
        task.setGroup(current.getGroup());
        return taskRepository.save(task);
    }

    @Override
    public Task updateTask(Task task) {
        Task existingTask = taskRepository.findById(task.getId())
            .orElseThrow(() -> new RuntimeException("Task not found with id: " + task.getId()));

        existingTask.setTitle(task.getTitle());
        existingTask.setDescription(task.getDescription());
        existingTask.setType(task.getType());
        existingTask.setStatus(task.getStatus());
        existingTask.setAssignedTo(task.getAssignedTo());
        if (task.getGroup() != null) {
            existingTask.setGroup(task.getGroup());
        }

        return taskRepository.save(existingTask);
    }

    @Override
    public Optional<Task> updateTask(Long id, Task updatedTask) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Task existing = getTaskById(id).orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

        if (updatedTask.getTitle() != null) existing.setTitle(updatedTask.getTitle());
        if (updatedTask.getDescription() != null) existing.setDescription(updatedTask.getDescription());
        if (updatedTask.getType() != null) existing.setType(updatedTask.getType());
        if (updatedTask.getStatus() != null) existing.setStatus(updatedTask.getStatus());
        existing.setAssignedTo(updatedTask.getAssignedTo());
        existing.setUpdatedOn(java.time.LocalDateTime.now());

        if (hasAnyRole(auth, "ADMIN", "MANAGER")) {
            if (updatedTask.getGroup() != null && updatedTask.getGroup().getId() != null) {
                existing.setGroup(updatedTask.getGroup());
            }
        } else {
            User current = currentUser();
            if (current == null || current.getGroup() == null) {
                throw new AccessDeniedException("No group assigned");
            }
            existing.setGroup(current.getGroup());
        }

        return Optional.of(taskRepository.save(existing));
    }

    @Override
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    @Override
    public Optional<Task> getTaskById(Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
            throw new AccessDeniedException("Unauthorized");
        }

        if (hasAnyRole(auth, "ADMIN", "MANAGER")) {
            return taskRepository.findById(id);
        }

        User current = currentUser();
        Long groupId = current != null && current.getGroup() != null ? current.getGroup().getId() : null;
        if (groupId == null) throw new AccessDeniedException("No group assigned");

        Optional<Task> scoped = taskRepository.findByIdAndGroupId(id, groupId);
        if (scoped.isEmpty()) throw new AccessDeniedException("Not in your group");
        return scoped;
    }

    @Override
    public List<TaskDto> list() {
        List<Task> tasks = getTasks();
        Map<Long,String> names = usernamesByIds(tasks);
        return tasks.stream().map(t -> toDto(t, names)).toList();
    }

    @Override
    public List<TaskDto> listByGroup(Long groupId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        List<Task> tasks;
        if (hasAnyRole(auth, "ADMIN", "MANAGER")) {
            tasks = taskRepository.findByGroupId(groupId);
        } else {
            User current = currentUser();
            Long currentGroupId = current != null && current.getGroup() != null ? current.getGroup().getId() : null;
            if (currentGroupId == null) throw new AccessDeniedException("No group assigned");
            if (!currentGroupId.equals(groupId)) throw new AccessDeniedException("Not in your group");
            tasks = taskRepository.findByGroupId(groupId);
        }

        Map<Long,String> names = usernamesByIds(tasks);
        return tasks.stream().map(t -> toDto(t, names)).toList();
    }



}
