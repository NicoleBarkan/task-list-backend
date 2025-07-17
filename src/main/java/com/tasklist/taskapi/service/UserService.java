package com.tasklist.taskapi.service;

import com.tasklist.taskapi.model.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> getAllUsers();
    Optional<User> getUserById(Long id);
}
