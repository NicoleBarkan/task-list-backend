package com.tasklist.taskapi.controller;

import com.tasklist.taskapi.dto.LoginRequest;
import com.tasklist.taskapi.dto.RegisterRequestDto;
import com.tasklist.taskapi.model.User;
import com.tasklist.taskapi.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                Map<String, Object> response = new HashMap<>();
                response.put("userId", user.getId());
                response.put("firstName", user.getFirstName());
                response.put("lastName", user.getLastName());
                return ResponseEntity.ok(response);
            }
        }
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .header("Content-Type", "application/json")
            .body(Map.of("message", "Invalid username or password"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDto request) {
        if (userRepository.findByUsername(request.username).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username already taken. Choose another one."));
        }

        User newUser = new User();
        newUser.setUsername(request.username);
        newUser.setPassword(passwordEncoder.encode(request.password));
        newUser.setFirstName(request.firstName);
        newUser.setLastName(request.lastName);
        userRepository.save(newUser);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUserDetails(@PathVariable Long id) {
        return userRepository.findById(id)
            .map(user -> Map.of("firstName", user.getFirstName(), "lastName", user.getLastName()))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
