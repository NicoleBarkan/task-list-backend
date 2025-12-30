package com.tasklist.taskapi.repository;

import com.tasklist.taskapi.model.Role;
import com.tasklist.taskapi.model.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
  Optional<RoleEntity> findByName(Role name);
}
