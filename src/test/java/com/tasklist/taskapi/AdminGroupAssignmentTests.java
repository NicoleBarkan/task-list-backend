package com.tasklist.taskapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tasklist.taskapi.model.Group;
import com.tasklist.taskapi.model.Role;
import com.tasklist.taskapi.model.Task;
import com.tasklist.taskapi.repository.TaskRepository;
import com.tasklist.taskapi.repository.UserRepository;
import com.tasklist.taskapi.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminGroupAssignmentTests {

    @Autowired MockMvc mvc;
    @Autowired TestData td;
    @Autowired TaskRepository taskRepo;
    @Autowired UserRepository userRepo;
    @Autowired ObjectMapper om;

    Group g1, g2;
    Long uId;

    @BeforeEach
    void setup() {
        g1 = td.group("G1");
        g2 = td.group("G2");
        var u = td.user("vasya", Set.of(Role.USER), g1);
        uId = u.getId();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void adminCanAssignUserToGroupAndCreateAndMoveTask() throws Exception {
        mvc.perform(put("/api/users/{id}/group/{gid}", uId, g2.getId()))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.id").value(uId))
           .andExpect(jsonPath("$.groupId").value(g2.getId()));

        String createBody = """
        {
          "title":"AdminTask",
          "description":"by admin",
          "type":"TASK",
          "status":"OPEN",
          "group":{"id":%d}
        }
        """.formatted(g2.getId());

        String createdJson = mvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.title").value("AdminTask"))
           .andExpect(jsonPath("$.groupId").value(g2.getId()))
           .andReturn().getResponse().getContentAsString();

        var created = om.readTree(createdJson);
        long taskId = created.get("id").asLong();

        String updateBody = """
        { "group": { "id": %d } }
        """.formatted(g1.getId());

        mvc.perform(put("/api/tasks/{id}", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBody))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.id").value(taskId))
           .andExpect(jsonPath("$.groupId").value(g1.getId()));

        Task t = taskRepo.findById(taskId).orElseThrow();
        assertThat(t.getGroup().getId()).isEqualTo(g1.getId());
    }
}
