package com.tasklist.taskapi.service.impl;

import com.tasklist.taskapi.model.User;
import com.tasklist.taskapi.repository.UserRepository;
import com.tasklist.taskapi.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
