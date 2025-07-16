package com.tasklist.taskapi.controller;

import com.tasklist.taskapi.dto.LoginRequest;
import com.tasklist.taskapi.model.User;
import com.tasklist.taskapi.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {
    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(request.getPassword())) {
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

    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUserDetails(@PathVariable Long id) {
        return userRepository.findById(id)
            .map(user -> Map.of("firstName", user.getFirstName(), "lastName", user.getLastName()))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
