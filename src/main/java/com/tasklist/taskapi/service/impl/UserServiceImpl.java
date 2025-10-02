package com.tasklist.taskapi.service.impl;

import com.tasklist.taskapi.model.User;
import com.tasklist.taskapi.dto.RegisterRequestDto;
import com.tasklist.taskapi.model.Role;
import com.tasklist.taskapi.model.Group;
import com.tasklist.taskapi.repository.GroupRepository;
import com.tasklist.taskapi.repository.UserRepository;
import com.tasklist.taskapi.service.UserService;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GroupRepository groupRepository; 

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, GroupRepository groupRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.groupRepository = groupRepository;
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

        if (user.getRole() == null) {
            user.setRole(new HashSet<>()); 
        }
        user.getRole().add(Role.USER); 

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
        return Optional.of(userRepository.save(user));
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
