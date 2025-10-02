package com.tasklist.taskapi.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.ResponseEntity;
import com.tasklist.taskapi.controller.TaskController;
import com.tasklist.taskapi.dto.TaskDto;
import com.tasklist.taskapi.model.Group;
import com.tasklist.taskapi.model.Task;
import com.tasklist.taskapi.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TaskVisibilityStandaloneTest {

  MockMvc mvc;
  ObjectMapper om = new ObjectMapper();
  TaskService taskService = Mockito.mock(TaskService.class);

  @ControllerAdvice
  static class TestAdvice {
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<String> handle(AccessDeniedException ex) {
      return ResponseEntity.status(403).body(ex.getMessage());
    }
  }

  @BeforeEach
  void setup() {
    var taskController = new TaskController(taskService);
    mvc = MockMvcBuilders
            .standaloneSetup(taskController)
            .setControllerAdvice(new TestAdvice()) 
            .build();
  }

  @Test
  void userSeesOnlyOwnGroupTasks_andForbiddenOnOtherGroupTask() throws Exception {
    long g1 = 1L;
    long t1 = 10L;
    long t2 = 20L;

    Task ent1 = taskEntity(t1, g1, "T1", null);
    TaskDto dto1 = TaskDto.from(ent1, null);

    when(taskService.list()).thenReturn(List.of(dto1));

    mvc.perform(get("/api/tasks"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[*].id", hasItem((int) t1)))
        .andExpect(jsonPath("$[*].id", not(hasItem((int) t2))));

    when(taskService.getTaskById(t2)).thenThrow(new AccessDeniedException("Not in your group"));
    mvc.perform(get("/api/tasks/" + t2)).andExpect(status().isForbidden());

    when(taskService.getTaskById(t1)).thenReturn(Optional.of(ent1));
    mvc.perform(get("/api/tasks/" + t1))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value((int) t1))
        .andExpect(jsonPath("$.groupId").value((int) g1))
        .andExpect(jsonPath("$.title").value("T1"));
  }

  private Task taskEntity(long id, long groupId, String title, Long assignedTo) {
    var g = new Group(); g.setId(groupId);
    var t = new Task();
    t.setId(id);
    t.setGroup(g);
    t.setTitle(title);
    t.setAssignedTo(assignedTo);
    return t;
  }
}
