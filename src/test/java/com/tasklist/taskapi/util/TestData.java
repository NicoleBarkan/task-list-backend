package com.tasklist.taskapi.util;

import com.tasklist.taskapi.model.*;
import com.tasklist.taskapi.repository.*;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class TestData {
    private final GroupRepository groupRepo;
    private final UserRepository userRepo;
    private final TaskRepository taskRepo;

    public TestData(GroupRepository groupRepo, UserRepository userRepo, TaskRepository taskRepo) {
        this.groupRepo = groupRepo;
        this.userRepo = userRepo;
        this.taskRepo = taskRepo;
    }

    public Group group(String name) {
        return groupRepo.findByName(name).orElseGet(() -> {
            Group g = new Group();
            g.setName(name);
            g.setDescription(name + " desc");
            return groupRepo.save(g);
        });
    }

    public User user(String username, Set<Role> roles, Group g) {
        User u = userRepo.findByUsername(username).orElseGet(User::new);

        if (u.getId() == null) {
            u.setUsername(username);
        }
        u.setFirstName(username + "_fn");
        u.setLastName(username + "_ln");
        u.setPassword("{noop}pwd");
        u.setGroup(g);
        u.setRole(roles); 

        return userRepo.save(u);
    }

    public Task task(String title, Group g, Long assignedTo) {
        Task t = new Task();
        t.setTitle(title);
        t.setDescription(title + " desc");
        t.setType(TaskType.TASK);
        t.setStatus(TaskStatus.OPEN);
        t.setAssignedTo(assignedTo);
        t.setGroup(g);
        return taskRepo.save(t);
    }
}
