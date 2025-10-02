package com.tasklist.taskapi.controller;

import com.tasklist.taskapi.dto.UserDto;
import com.tasklist.taskapi.dto.UserMeDto;
import com.tasklist.taskapi.model.Role;
import com.tasklist.taskapi.model.User;
import com.tasklist.taskapi.security.SecurityUtils;
import com.tasklist.taskapi.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) { this.userService = userService; }

    @GetMapping
    public List<UserDto> getUsers(@RequestParam(name = "groupId", required = false) Long groupId) {
        var users = (groupId == null)
                ? userService.getAllUsers()
                : userService.getUsersByGroupId(groupId);
        return users.stream().map(UserDto::from).toList();
    }

    @GetMapping("/me")
    public ResponseEntity<UserMeDto> me(Authentication auth) {
    if (auth == null || !auth.isAuthenticated()) return ResponseEntity.status(401).build();
    var username = auth.getName();
    var dto = new UserMeDto();
    dto.username = username;
    dto.role = SecurityUtils.roleNames(auth, true);

    return userService.findByUsername(username)
        .map(u -> { dto.id = u.getId(); dto.firstName = u.getFirstName(); dto.lastName = u.getLastName();
                    dto.groupId = u.getGroup() != null ? u.getGroup().getId() : null; return dto; })
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.ok(dto)); 
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER') or @userSecurity.isSelf(authentication, #id)")
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(UserDto::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/role")
    public ResponseEntity<UserDto> updateRole(@PathVariable Long id, @RequestBody Set<Role> newRole) {
        return userService.getUserById(id).map(user -> {
            newRole.add(Role.USER);
            user.setRole(newRole);
            User saved = userService.saveUser(user);
            return ResponseEntity.ok(UserDto.from(saved));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/group/{groupId}")
    public ResponseEntity<UserDto> assignGroup(@PathVariable Long id, @PathVariable Long groupId) {
        return userService.assignGroup(id, groupId)
                .map(UserDto::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER') or @userSecurity.isSelf(authentication, #id)")
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Long id,
            @RequestBody UserDto payload
    ) {
        return userService.getUserById(id).map(user -> {
            if (payload.username() != null && !payload.username().isBlank()) {
                user.setUsername(payload.username().trim());
            }
            if (payload.password() != null && !payload.password().isBlank()) {
                userService.encodeAndSetPassword(user, payload.password());
            }

            try {
                boolean canChangeRole = org.springframework.security.core.context.SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getAuthorities()
                        .stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MANAGER"));
                if (canChangeRole && payload.role() != null && !payload.role().isEmpty()) {
                    Set<Role> roles = payload.role().stream()
                            .map(Role::valueOf)
                            .collect(Collectors.toSet());
                    roles.add(Role.USER);
                    user.setRole(roles);
                }
            } catch (Exception ignored) {}

            User saved = userService.saveUser(user);
            return ResponseEntity.ok(UserDto.from(saved));
        }).orElse(ResponseEntity.notFound().build());
    }


    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isSelf(authentication, #id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        return userService.getUserById(id).map(u -> {
            userService.deleteUserById(id);
            return ResponseEntity.noContent().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}