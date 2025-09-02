package com.tasklist.taskapi.controller;

import com.tasklist.taskapi.dto.LoginRequestDto;
import com.tasklist.taskapi.dto.RegisterRequestDto;
import com.tasklist.taskapi.model.User;
import com.tasklist.taskapi.repository.UserRepository;
import com.tasklist.taskapi.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder,  UserService userService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request) {
        String username = Optional.ofNullable(request.getUsername())
                                .map(String::trim)
                                .orElse("");
        String rawPassword = request.getPassword();

        Optional<User> userOpt = username.isEmpty()
            ? Optional.empty()
            : userRepository.findByUsernameIgnoreCase(username);

        if (userOpt.isEmpty() || rawPassword == null ||
            !passwordEncoder.matches(rawPassword, userOpt.get().getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        User user = userOpt.get();
        return ResponseEntity.ok(Map.of(
            "userId", user.getId(),
            "firstName", user.getFirstName(),
            "lastName", user.getLastName(),
            "role", user.getRole()
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDto request) {
        if (userRepository.findByUsernameIgnoreCase(request.username).isPresent()) {
            throw new IllegalArgumentException("Username already taken. Choose another one.");
        }

        User saved = userService.registerUser(request);

        return ResponseEntity.status(201).body(Map.of(
            "id", saved.getId(),
            "username", saved.getUsername(),
            "firstName", saved.getFirstName(),
            "lastName", saved.getLastName(),
            "role", saved.getRole()
        ));
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUserDetails(@PathVariable Long id) {
        return userRepository.findById(id)
            .map(user -> Map.of(
                "id", user.getId(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "username", user.getUsername(),
                "role", user.getRole()
            ))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
