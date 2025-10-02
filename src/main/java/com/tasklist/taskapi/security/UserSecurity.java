package com.tasklist.taskapi.security;

import com.tasklist.taskapi.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("userSecurity")
public class UserSecurity {
    private final UserService userService;

    public UserSecurity(UserService userService) {
        this.userService = userService;
    }

    public boolean isSelf(Authentication authentication, Long userId) {
        if (authentication == null || userId == null) return false;
        return userService.getUserById(userId)
                .map(u -> u.getUsername().equals(authentication.getName()))
                .orElse(false);
    }
}
