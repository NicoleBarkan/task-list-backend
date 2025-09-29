package com.tasklist.taskapi.service;

import com.tasklist.taskapi.dto.GroupDto;
import com.tasklist.taskapi.model.Group;
import com.tasklist.taskapi.repository.GroupRepository;
import com.tasklist.taskapi.service.impl.GroupServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupServiceImplTest {

  @Mock GroupRepository repo;
  @InjectMocks GroupServiceImpl service;

  @Test
  void create_returnsDto() {
    var dto = new GroupDto();
    dto.name = "Test";
    dto.description = "Desc";

    when(repo.save(any(Group.class))).thenAnswer(inv -> {
      Group g = inv.getArgument(0);
      g.setId(1L);
      return g;
    });

    var result = service.create(dto);

    assertThat(result.id).isEqualTo(1L);
    assertThat(result.name).isEqualTo("Test");
  }

  @Test
  void update_updatesEntity() {
    var g = new Group();
    g.setId(1L);
    g.setName("Old");

    when(repo.findById(1L)).thenReturn(Optional.of(g));
    when(repo.save(any(Group.class))).thenAnswer(inv -> inv.getArgument(0));

    var dto = new GroupDto();
    dto.name = "New";

    var result = service.update(1L, dto);

    assertThat(result.name).isEqualTo("New");
  }
}
