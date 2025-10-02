package com.tasklist.taskapi.service;

import com.tasklist.taskapi.dto.GroupDto;

import java.util.List;

public interface GroupService {
    GroupDto create(GroupDto dto);
    GroupDto update(Long id, GroupDto dto);
    void delete(Long id);
    List<GroupDto> list();
    GroupDto get(Long id);
}
