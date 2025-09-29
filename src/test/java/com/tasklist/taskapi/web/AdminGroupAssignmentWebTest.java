package com.tasklist.taskapi.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tasklist.taskapi.controller.TaskController;
import com.tasklist.taskapi.controller.UserController;
import com.tasklist.taskapi.model.Group;
import com.tasklist.taskapi.model.Role;
import com.tasklist.taskapi.model.Task;
import com.tasklist.taskapi.model.User;
import com.tasklist.taskapi.service.TaskService;
import com.tasklist.taskapi.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminGroupAssignmentStandaloneTest {

  MockMvc mvc;
  ObjectMapper om = new ObjectMapper();

  TaskService taskService = Mockito.mock(TaskService.class);
  UserService userService = Mockito.mock(UserService.class);

  @BeforeEach
  void setup() {
    var userController = new UserController(userService);
    var taskController = new TaskController(taskService);
    mvc = MockMvcBuilders.standaloneSetup(userController, taskController).build();
  }

  @Test
  void admin_can_assign_user_to_group_and_create_and_move_task() throws Exception {
    long userId = 100L;
    long g1 = 1L;
    long g2 = 2L;

    var group2 = group(g2);
    var group1 = group(g1);

    var user = new User();
    user.setId(userId);
    user.setUsername("vasya");
    user.setRole(Set.of(Role.USER, Role.ADMIN));
    user.setGroup(group2);

    when(userService.assignGroup(eq(userId), eq(g2))).thenReturn(Optional.of(user));

    mvc.perform(put("/api/users/{id}/group/{gid}", userId, g2))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is((int) userId)))
        .andExpect(jsonPath("$.groupId", is((int) g2)));

    var created = new Task();
    created.setId(777L);
    created.setTitle("AdminTask");
    created.setGroup(group2);
    when(taskService.addTask(any(Task.class))).thenReturn(created);

    String createBody = """
        {
          "title":"AdminTask",
          "description":"by admin",
          "type":"TASK",
          "status":"OPEN",
          "group":{"id":%d}
        }
        """.formatted(g2);

    mvc.perform(post("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(createBody))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value("AdminTask"))
        .andExpect(jsonPath("$.groupId").value((int) g2));

    var updated = new Task();
    updated.setId(777L);
    updated.setTitle("AdminTask");
    updated.setGroup(group1);
    when(taskService.updateTask(eq(777L), any(Task.class))).thenReturn(Optional.of(updated));

    String updateBody = """
        { "group": { "id": %d } }
        """.formatted(g1);

    mvc.perform(put("/api/tasks/{id}", 777L)
            .contentType(MediaType.APPLICATION_JSON)
            .content(updateBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(777))
        .andExpect(jsonPath("$.groupId").value((int) g1));
  }

  private Group group(long id) {
    var g = new Group(); g.setId(id); return g;
  }
}
