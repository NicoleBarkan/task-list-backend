package com.tasklist.taskapi.dto;

public record TaskUpdateDto(
        String title,
        String description,
        String type,
        String status,
        Long groupId,
        Long assignedToId
) {}
