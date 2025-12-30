package com.tasklist.taskapi.service.impl;

import com.tasklist.taskapi.config.GroupConstants;
import com.tasklist.taskapi.dto.GroupDto;
import com.tasklist.taskapi.model.Group;
import com.tasklist.taskapi.repository.GroupRepository;
import com.tasklist.taskapi.repository.TaskRepository;
import com.tasklist.taskapi.repository.UserRepository;
import com.tasklist.taskapi.service.GroupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class GroupServiceImpl implements GroupService {
    private final GroupRepository repo;
    private final UserRepository userRepo;
    private final TaskRepository taskRepo;

    private final Long defaultGroupId = GroupConstants.UNASSIGNED_GROUP_ID;

    public GroupServiceImpl(
            GroupRepository repo,
            UserRepository userRepo,
            TaskRepository taskRepo
    ) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.taskRepo = taskRepo;
    }

    private GroupDto toDto(Group g) {
        GroupDto dto = new GroupDto();
        dto.id = g.getId();
        dto.title = g.getTitle();
        dto.description = g.getDescription();
        return dto;
    }

    private Group fromDto(GroupDto dto) {
        Group g = new Group();
        g.setTitle(dto.title);
        g.setDescription(dto.description);
        return g;
    }

    @Override
    public GroupDto create(GroupDto dto) {
        String title = dto.title == null ? "" : dto.title.trim();
        if (title.isEmpty()) throw new IllegalArgumentException("Group title must not be blank");
        if (repo.existsByTitle(title)) throw new IllegalArgumentException("Group title already exists: " + title);

        Group saved = repo.save(fromDto(dto));
        return toDto(saved);
    }

    @Override
    public GroupDto update(Long id, GroupDto dto) {
        Group existing = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + id));

        String title = dto.title == null ? "" : dto.title.trim();
        if (title.isEmpty()) throw new IllegalArgumentException("Group title must not be blank");

        if (!existing.getTitle().equalsIgnoreCase(title) && repo.existsByTitle(title)) {
            throw new IllegalArgumentException("Group title already exists: " + title);
        }

        existing.setTitle(title);
        existing.setDescription(dto.description);
        return toDto(repo.save(existing));
    }

    @Transactional
    @Override
    public void delete(Long id) {
        if (Objects.equals(id, defaultGroupId)) {
            throw new IllegalArgumentException("Cannot delete default group");
        }
        if (userRepo.existsByGroupId(id) || taskRepo.existsByGroupId(id)) {
            throw new IllegalArgumentException("The group contains users/tasks, deletion is prohibited");
        }
        repo.deleteById(id);
    }

    @Override
    public List<GroupDto> list() {
        return repo.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public GroupDto get(Long id) {
        return repo.findById(id).map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + id));
    }
}
