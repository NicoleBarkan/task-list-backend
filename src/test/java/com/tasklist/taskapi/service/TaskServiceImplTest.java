package com.tasklist.taskapi.service;

import com.tasklist.taskapi.model.Group;
import com.tasklist.taskapi.model.Task;
import com.tasklist.taskapi.model.User;
import com.tasklist.taskapi.repository.TaskRepository;
import com.tasklist.taskapi.repository.UserRepository;
import com.tasklist.taskapi.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

  @Mock TaskRepository taskRepo;
  @Mock UserRepository userRepo;
  @InjectMocks TaskServiceImpl service;

  @AfterEach
  void clearCtx() {
    SecurityContextHolder.clearContext();
  }

  private void authAs(String username, String... roles) {
    var auth = new UsernamePasswordAuthenticationToken(
        username, "N/A",
        Arrays.stream(roles)
            .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
            .map(SimpleGrantedAuthority::new)
            .toList()
    );
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @Test
  void list_as_admin_returns_all() {
    authAs("admin","ADMIN");
    when(taskRepo.findAll()).thenReturn(List.of(new Task(), new Task()));

    var list = service.getTasks();

    assertThat(list).hasSize(2);
  }

  @Test
  void list_as_user_without_group_returns_empty() {
    authAs("vasya","USER");
    when(userRepo.findByUsername("vasya")).thenReturn(Optional.of(new User()));

    var list = service.getTasks();

    assertThat(list).isEmpty();
  }

  @Test
  void addTask_user_sets_group_from_current_user() {
    var g = new Group(); g.setId(7L);
    var u = new User(); u.setUsername("vasya"); u.setGroup(g);

    authAs("vasya","USER");
    when(userRepo.findByUsername("vasya")).thenReturn(Optional.of(u));
    when(taskRepo.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

    var t = new Task();
    t.setTitle("T");

    var saved = service.addTask(t);

    assertThat(saved.getGroup().getId()).isEqualTo(7L);
  }

  @Test
  void addTask_user_without_group_denied() {
    authAs("vasya","USER");
    when(userRepo.findByUsername("vasya")).thenReturn(Optional.of(new User()));

    var t = new Task();
    t.setTitle("T");

    assertThatThrownBy(() -> service.addTask(t))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void updateTask_admin_can_move_between_groups() {
    authAs("admin","ADMIN");
    var existing = new Task(); existing.setId(1L); existing.setGroup(new Group());

    when(taskRepo.findById(1L)).thenReturn(Optional.of(existing));
    when(taskRepo.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

    var newGroup = new Group(); newGroup.setId(99L);
    var patch = new Task(); patch.setId(1L); patch.setGroup(newGroup);

    var out = service.updateTask(1L, patch).orElseThrow();

    assertThat(out.getGroup().getId()).isEqualTo(99L);
  }

    @Test
    void getTaskById_user_cannot_read_other_group() {
    var g10 = new Group(); g10.setId(10L);
    var u = new User(); u.setUsername("vasya"); u.setGroup(g10);

    authAs("vasya","USER");
    when(userRepo.findByUsername("vasya")).thenReturn(Optional.of(u));
    when(taskRepo.findByIdAndGroupId(1L, 10L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getTaskById(1L))
        .isInstanceOf(AccessDeniedException.class);

    verify(userRepo).findByUsername("vasya");
    verify(taskRepo).findByIdAndGroupId(1L, 10L);
    verifyNoMoreInteractions(taskRepo, userRepo);
    }

  @Test
  void deleteTask_deletes_from_repo() {
    service.deleteTask(123L);
  }
}
