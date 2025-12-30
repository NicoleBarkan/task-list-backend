package com.tasklist.taskapi.service.impl;

import com.tasklist.taskapi.config.GroupConstants;
import com.tasklist.taskapi.dto.TaskCreateDto;
import com.tasklist.taskapi.dto.TaskDto;
import com.tasklist.taskapi.dto.TaskUpdateDto;
import com.tasklist.taskapi.model.Group;
import com.tasklist.taskapi.model.Task;
import com.tasklist.taskapi.model.TaskStatus;
import com.tasklist.taskapi.model.TaskType;
import com.tasklist.taskapi.model.User;
import com.tasklist.taskapi.repository.GroupRepository;
import com.tasklist.taskapi.repository.TaskRepository;
import com.tasklist.taskapi.repository.UserRepository;
import com.tasklist.taskapi.service.TaskService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.tasklist.taskapi.security.SecurityUtils.hasAnyRole;

@Service
public class TaskServiceImpl implements TaskService {

    private static final long UNASSIGNED_GROUP_ID = GroupConstants.UNASSIGNED_GROUP_ID;

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    public TaskServiceImpl(TaskRepository taskRepository, UserRepository userRepository, GroupRepository groupRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
    }

    // -------- helpers --------

    private Authentication auth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private User currentUser() {
        Authentication auth = auth();
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

        if (username == null || "anonymousUser".equals(username)) return null;
        return userRepository.findByUsername(username).orElse(null);
    }

    private Group getUnassignedGroup() {
        return groupRepository.findById(UNASSIGNED_GROUP_ID)
                .orElseThrow(() -> new IllegalStateException("Unassigned group not found"));
    }

    private TaskType parseType(String s) {
        if (s == null || s.isBlank()) return null;
        return TaskType.valueOf(s.trim().toUpperCase());
    }

    private TaskStatus parseStatus(String s) {
        if (s == null || s.isBlank()) return null;
        return TaskStatus.valueOf(s.trim().toUpperCase());
    }

    private Map<Long, String> usernamesByIds(List<Task> tasks) {
        Set<Long> ids = tasks.stream()
                .map(Task::getAssignedTo)
                .filter(Objects::nonNull)
                .map(User::getId)
                .collect(Collectors.toSet());

        if (ids.isEmpty()) return Map.of();

        return userRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));
    }

    private TaskDto toDto(Task t, Map<Long, String> names) {
        Long uid = (t.getAssignedTo() != null) ? t.getAssignedTo().getId() : null;
        String username = (uid != null) ? names.get(uid) : null;
        return TaskDto.from(t, username);
    }

    private void ensureAuthenticated() {
        Authentication a = auth();
        if (a == null || !a.isAuthenticated() || "anonymousUser".equals(String.valueOf(a.getPrincipal()))) {
            throw new AccessDeniedException("Unauthorized");
        }
    }

    private void ensureUserHasGroup(User u) {
        if (u == null || u.getGroup() == null) {
            throw new AccessDeniedException("No group assigned");
        }
    }

    // -------- service methods --------

    @Override
    public List<TaskDto> list() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        List<Task> tasks;
        if (hasAnyRole(auth, "ADMIN", "MANAGER")) {
            tasks = taskRepository.findAll();
        } else {
            User current = currentUser();
            Long groupId = current != null && current.getGroup() != null ? current.getGroup().getId() : null;
            if (groupId == null) return List.of();
            tasks = taskRepository.findByGroupId(groupId);
        }

        Map<Long,String> names = usernamesByIds(tasks);
        return tasks.stream().map(t -> toDto(t, names)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDto> listByGroup(Long groupId) {
        ensureAuthenticated();
        Authentication a = auth();

        if (!hasAnyRole(a, "ADMIN", "MANAGER")) {
            User current = currentUser();
            ensureUserHasGroup(current);
            if (!Objects.equals(current.getGroup().getId(), groupId)) {
                throw new AccessDeniedException("Not in your group");
            }
        }

        List<Task> tasks = taskRepository.findByGroupId(groupId);
        Map<Long, String> names = usernamesByIds(tasks);
        return tasks.stream().map(t -> toDto(t, names)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TaskDto> getTaskById(Long id) {
        ensureAuthenticated();
        Authentication a = auth();

        Optional<Task> taskOpt;
        if (hasAnyRole(a, "ADMIN", "MANAGER")) {
            taskOpt = taskRepository.findById(id);
        } else {
            User current = currentUser();
            ensureUserHasGroup(current);
            taskOpt = taskRepository.findByIdAndGroupId(id, current.getGroup().getId());
            if (taskOpt.isEmpty()) throw new AccessDeniedException("Not in your group");
        }

        return taskOpt.map(t -> {
            String name = (t.getAssignedTo() != null) ? t.getAssignedTo().getUsername() : null;
            return TaskDto.from(t, name);
        });
    }

    @Override
    @Transactional
    public TaskDto addTask(TaskCreateDto dto) {
        ensureAuthenticated();
        Authentication a = auth();

        // normalize incoming ids
        Long requestedGroupId = (dto.groupId() != null && dto.groupId() > 0) ? dto.groupId() : null;
        Long assigneeId = (dto.assignedToId() != null && dto.assignedToId() > 0) ? dto.assignedToId() : null;

        // resolve assignee (optional)
        User assignee = null;
        if (assigneeId != null) {
            assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + assigneeId));

            Group assigneeGroup = assignee.getGroup();
            if (assigneeGroup == null) throw new IllegalStateException("Assignee has no group");
            if (assigneeGroup.getId() == UNASSIGNED_GROUP_ID) {
                throw new IllegalArgumentException("Cannot assign tasks to users in 'Unassigned' group");
            }
        }

        // determine group
        Group group;
        if (assignee != null) {
            group = assignee.getGroup(); // task group follows assignee group
        } else if (requestedGroupId != null) {
            group = groupRepository.findById(requestedGroupId)
                    .orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + requestedGroupId));
        } else {
            group = getUnassignedGroup();
        }

        // access control for non-admin/manager
        if (!hasAnyRole(a, "ADMIN", "MANAGER")) {
            User current = currentUser();
            ensureUserHasGroup(current);
            if (!Objects.equals(group.getId(), current.getGroup().getId())) {
                throw new AccessDeniedException("Cannot create task in another group");
            }
            // if assignee exists — must be from same group
            if (assignee != null && !Objects.equals(assignee.getGroup().getId(), current.getGroup().getId())) {
                throw new AccessDeniedException("Cannot assign user from another group");
            }
        }

        Task task = new Task(
                dto.title(),
                dto.description(),
                parseType(dto.type()),
                parseStatus(dto.status()),
                group
        );
        task.setAssignedTo(assignee);

        Task saved = taskRepository.save(task);

        String assigneeName = (assignee != null) ? assignee.getUsername() : null;
        return TaskDto.from(saved, assigneeName);
    }

    @Override
    @Transactional
    public Optional<TaskDto> updateTask(Long id, TaskUpdateDto dto) {
        ensureAuthenticated();
        Authentication a = auth();

        Task existing = taskRepository.findById(id).orElse(null);
        if (existing == null) return Optional.empty();

        // access control for USER
        if (!hasAnyRole(a, "ADMIN", "MANAGER")) {
            User current = currentUser();
            ensureUserHasGroup(current);

            // allow update only inside own group
            if (existing.getGroup() == null || !Objects.equals(existing.getGroup().getId(), current.getGroup().getId())) {
                throw new AccessDeniedException("Not in your group");
            }
        }

        // fields update
        if (dto.title() != null) existing.setTitle(dto.title());
        if (dto.description() != null) existing.setDescription(dto.description());
        if (dto.type() != null) existing.setType(parseType(dto.type()));
        if (dto.status() != null) existing.setStatus(parseStatus(dto.status()));

        // normalize ids
        Long requestedGroupId = (dto.groupId() != null && dto.groupId() > 0) ? dto.groupId() : null;
        Long assigneeId = (dto.assignedToId() != null && dto.assignedToId() > 0) ? dto.assignedToId() : null;

        // resolve assignee (optional)
        User newAssignee = null;
        if (assigneeId != null) {
            newAssignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + assigneeId));

            Group assigneeGroup = newAssignee.getGroup();
            if (assigneeGroup == null) throw new IllegalStateException("Assignee has no group");
            if (assigneeGroup.getId() == UNASSIGNED_GROUP_ID) {
                throw new IllegalArgumentException("Cannot assign tasks to users in 'Unassigned' group");
            }
        }

        if (hasAnyRole(a, "ADMIN", "MANAGER")) {
            // ADMIN/MANAGER logic:
            // if assignee set -> task group becomes assignee group (unless requested group conflicts -> then unassign)
            if (newAssignee != null) {
                Group assigneeGroup = newAssignee.getGroup();

                if (requestedGroupId != null && !Objects.equals(requestedGroupId, assigneeGroup.getId())) {
                    // mismatch: keep requested group, clear assignee
                    existing.setAssignedTo(null);
                    Group g = groupRepository.findById(requestedGroupId)
                            .orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + requestedGroupId));
                    existing.setGroup(g);
                } else {
                    existing.setAssignedTo(newAssignee);
                    existing.setGroup(assigneeGroup);
                }
            } else {
                // unassign
                existing.setAssignedTo(null);

                if (requestedGroupId != null) {
                    Group g = groupRepository.findById(requestedGroupId)
                            .orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + requestedGroupId));
                    existing.setGroup(g);
                } else if (existing.getGroup() == null) {
                    existing.setGroup(getUnassignedGroup());
                }
            }
        } else {
            // USER logic: group is always current user's group; assignee must be same group if set
            User current = currentUser();
            ensureUserHasGroup(current);

            if (newAssignee != null && !Objects.equals(newAssignee.getGroup().getId(), current.getGroup().getId())) {
                throw new AccessDeniedException("Cannot assign user from another group");
            }

            existing.setGroup(current.getGroup());
            existing.setAssignedTo(newAssignee);
        }

        Task saved = taskRepository.save(existing);
        String assigneeName = (saved.getAssignedTo() != null) ? saved.getAssignedTo().getUsername() : null;
        return Optional.of(TaskDto.from(saved, assigneeName));
    }

    @Override
    @Transactional
    public boolean deleteTask(Long id) {
        ensureAuthenticated();
        Authentication a = auth();

        if (hasAnyRole(a, "ADMIN", "MANAGER")) {
            if (!taskRepository.existsById(id)) return false;
            taskRepository.deleteById(id);
            return true;
        }

        // USER: can delete only inside own group
        User current = currentUser();
        ensureUserHasGroup(current);

        Optional<Task> scoped = taskRepository.findByIdAndGroupId(id, current.getGroup().getId());
        if (scoped.isEmpty()) return false;

        taskRepository.deleteById(id);
        return true;
    }
}
