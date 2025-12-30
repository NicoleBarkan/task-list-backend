package com.tasklist.taskapi.security;

import com.tasklist.taskapi.model.User;
import com.tasklist.taskapi.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;


@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository users;
    public CustomUserDetailsService(UserRepository users) { this.users = users; }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = users.findByUsernameIgnoreCase(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        var authorities = SecurityUtils.toAuthorities(u.getRole());

        return new org.springframework.security.core.userdetails.User(
            u.getUsername(),
            u.getPassword(),
            authorities 
        );
    }
}

