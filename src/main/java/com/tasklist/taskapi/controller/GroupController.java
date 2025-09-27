package com.tasklist.taskapi.controller;

import com.tasklist.taskapi.dto.GroupDto;
import com.tasklist.taskapi.service.GroupService;
import com.tasklist.taskapi.dto.UserDto;
import com.tasklist.taskapi.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService service;
    private final UserService userService;

    public GroupController(GroupService service, UserService userService) {
        this.service = service;
        this.userService = userService;
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<GroupDto> create(@Valid @RequestBody GroupDto dto) {
        GroupDto saved = service.create(dto);
        URI location = URI.create("/api/groups/" + saved.id);
        return ResponseEntity.created(location).body(saved);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public GroupDto update(@PathVariable Long id, @Valid @RequestBody GroupDto dto) {
        return service.update(id, dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { service.delete(id); }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping
    public List<GroupDto> list() { return service.list(); }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<GroupDto> get(@PathVariable Long id) {
        GroupDto dto = service.get(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/{id}/users")
    public List<UserDto> users(@PathVariable Long id) {
        return userService.getUsersByGroupId(id)
                .stream()
                .map(UserDto::from)
                .toList();
    }
}

