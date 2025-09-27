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
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class TaskVisibilityTests {

    @Autowired MockMvc mvc;
    @Autowired TestData td;
    @Autowired TaskRepository taskRepo;
    @Autowired UserRepository userRepo;
    @Autowired ObjectMapper om;

    Group g1, g2;
    Task t1_g1, t2_g2;

    @BeforeEach
    void setup() {
        g1 = td.group("G1");
        g2 = td.group("G2");

        var u1 = td.user("u1", Set.of(Role.USER), g1);
        var u2 = td.user("u2", Set.of(Role.USER), g2);
        td.user("admin", Set.of(Role.ADMIN, Role.USER), g1);

        t1_g1 = td.task("T1", g1, u1.getId());
        t2_g2 = td.task("T2", g2, u2.getId());
    }

    @Test
    @WithMockUser(username = "u1", roles = {"USER"})
    void userSeesOnlyOwnGroupTasks() throws Exception {
        mvc.perform(get("/api/tasks"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[*].id", hasItem(t1_g1.getId().intValue())))
        .andExpect(jsonPath("$[*].id", not(hasItem(t2_g2.getId().intValue()))));

        mvc.perform(get("/api/tasks/" + t2_g2.getId()))
        .andExpect(status().isForbidden());
        
        mvc.perform(get("/api/tasks/" + t1_g1.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(t1_g1.getId()))
        .andExpect(jsonPath("$.groupId").value(g1.getId()))
        .andExpect(jsonPath("$.title").value("T1"));
    }

    @Test
    @WithMockUser(username = "u1", roles = {"USER"})
    void userCannotCreateTask() throws Exception {
        String body = """
        { "title":"X", "description":"x", "type":"TASK", "status":"OPEN", "group":{"id":%d} }
        """.formatted(g1.getId());

        mvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
           .andExpect(status().isForbidden());
    }
}
