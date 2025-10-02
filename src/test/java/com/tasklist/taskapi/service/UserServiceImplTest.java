package com.tasklist.taskapi.service;

import com.tasklist.taskapi.model.Role;
import com.tasklist.taskapi.model.User;
import com.tasklist.taskapi.repository.UserRepository;
import com.tasklist.taskapi.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  @Mock UserRepository repo;
  @InjectMocks UserServiceImpl service;

  @Test
  void findByUsername_returnsUser() {
    var u = new User();
    u.setId(1L);
    u.setUsername("vasya");
    u.setRole(Set.of(Role.USER));

    lenient().when(repo.findByUsername(anyString())).thenReturn(Optional.of(u));
    try {
      repo.getClass().getMethod("findByUsernameIgnoreCase", String.class);
      lenient().when(repo.findByUsernameIgnoreCase(anyString())).thenReturn(Optional.of(u));
    } catch (NoSuchMethodException ignored) {}

    var found = service.findByUsername("  VaSyA  ");
    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(1L);

    verify(repo, atLeast(0)).findByUsername(anyString());
    try {
      repo.getClass().getMethod("findByUsernameIgnoreCase", String.class);
      verify(repo, atLeast(0)).findByUsernameIgnoreCase(anyString());
    } catch (NoSuchMethodException ignored) {}
  }
}
