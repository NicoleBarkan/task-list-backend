package com.tasklist.taskapi.security;

import com.tasklist.taskapi.model.User;
import com.tasklist.taskapi.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository users;
    public CustomUserDetailsService(UserRepository users) { this.users = users; }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = users.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        var authorities = u.getRole().stream() 
            .map(r -> new SimpleGrantedAuthority(
                r.name().startsWith("ROLE_") ? r.name() : "ROLE_" + r.name()))
            .collect(Collectors.toSet());

        return new org.springframework.security.core.userdetails.User(
            u.getUsername(),
            u.getPassword(),
            authorities 
        );
    }
}

