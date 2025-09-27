package com.tasklist.taskapi.service;

import com.tasklist.taskapi.dto.RegisterRequestDto;
import com.tasklist.taskapi.model.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> getAllUsers();
    List<User> getUsersByGroupId(Long groupId);  
    Optional<User> getUserById(Long id);
    User saveUser(User user);
    User registerUser(RegisterRequestDto request);
    Optional<User> findByUsername(String username);
    Optional<User> assignGroup(Long userId, Long groupId);
    void encodeAndSetPassword(User user, String rawPassword);
    void deleteUserById(Long id);
}
