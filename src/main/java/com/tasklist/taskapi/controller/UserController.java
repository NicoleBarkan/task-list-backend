package com.tasklist.taskapi.controller;

import com.tasklist.taskapi.model.Role;
import com.tasklist.taskapi.model.User;
import com.tasklist.taskapi.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<User> updateRole(@PathVariable Long id, @RequestBody Set<Role> newRole) {
        return userService.getUserById(id).map(user -> {
            newRole.add(Role.USER);
            user.setRole(newRole);
            userService.saveUser(user);
            return ResponseEntity.ok(user);
        }).orElse(ResponseEntity.notFound().build());
    }
}