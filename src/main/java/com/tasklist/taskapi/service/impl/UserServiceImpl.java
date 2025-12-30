package com.tasklist.taskapi.service.impl;

import com.tasklist.taskapi.model.User;
import com.tasklist.taskapi.dto.RegisterRequestDto;
import com.tasklist.taskapi.model.Role;
import com.tasklist.taskapi.model.Group;
import com.tasklist.taskapi.repository.GroupRepository;
import com.tasklist.taskapi.repository.RoleRepository;
import com.tasklist.taskapi.repository.UserRepository;
import com.tasklist.taskapi.repository.TaskRepository;
import com.tasklist.taskapi.service.UserService;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private static final long UNASSIGNED_GROUP_ID = 1L;

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GroupRepository groupRepository; 
    private final RoleRepository roleRepository;

    public UserServiceImpl(TaskRepository taskRepository, UserRepository userRepository, PasswordEncoder passwordEncoder, GroupRepository groupRepository, RoleRepository roleRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.groupRepository = groupRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<User> getUsersByGroupId(Long groupId) {
        return userRepository.findByGroupId(groupId);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User registerUser(RegisterRequestDto request) {
        String username = request.username == null ? "" : request.username.trim();
        String firstName = request.firstName == null ? "" : request.firstName.trim();
        String lastName  = request.lastName == null ? "" : request.lastName.trim();

        if (username.isEmpty()) {
            throw new IllegalArgumentException("Username must not be blank");
        }
        if (userRepository.findByUsernameIgnoreCase(username).isPresent()) {
            throw new IllegalArgumentException("Username already taken. Choose another one.");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.password));
        user.setFirstName(firstName);
        user.setLastName(lastName);

        var userRole = roleRepository.findByName(Role.USER).orElseThrow(() -> new IllegalStateException("Role not found in DB: USER"));

        user.getRoles().clear();
        user.addRole(userRole);

        Group unassigned = groupRepository.findById(UNASSIGNED_GROUP_ID)
                .orElseThrow(() -> new IllegalStateException("Default group (Unassigned tasks) not found"));
        user.setGroup(unassigned);

        return userRepository.save(user);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        if (username == null) return Optional.empty();
        return userRepository.findByUsernameIgnoreCase(username.trim());
    }

    @Override
    @Transactional
    public Optional<User> assignGroup(Long userId, Long groupId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return Optional.empty();

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        User user = userOpt.get();
        user.setGroup(group);
        User saved = userRepository.save(user);

        if (Objects.equals(groupId, UNASSIGNED_GROUP_ID)) {
            taskRepository.unassignTasksForUser(userId, groupId);
        } else {
            taskRepository.updateGroupForAssignedUserTasks(userId, groupId);
        }

        return Optional.of(saved);
    }

    @Override
    public void encodeAndSetPassword(User user, String rawPassword) {
        user.setPassword(passwordEncoder.encode(rawPassword));
    }

    @Override
    @Transactional
    public void deleteUserById(Long id) {
        if (id == null) return;
        if (!userRepository.existsById(id)) return;
        userRepository.deleteById(id);
    }
    
}
