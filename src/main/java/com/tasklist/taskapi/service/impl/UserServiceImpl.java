package com.tasklist.taskapi.service.impl;

import com.tasklist.taskapi.model.User;
import com.tasklist.taskapi.dto.RegisterRequestDto;
import com.tasklist.taskapi.model.Role;
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

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public void saveUser(User user) {
        userRepository.save(user);
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
}
