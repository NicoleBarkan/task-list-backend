package com.tasklist.taskapi.repository;

import com.tasklist.taskapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameIgnoreCase(String username);
    List<User> findByGroupId(Long groupId);
    Optional<User> findByUsername(String username); 
    boolean existsByGroupId(Long groupId);
}
