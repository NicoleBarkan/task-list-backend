package com.tasklist.taskapi.dto;

public record TaskCreateDto(
    String title,
    String description,
    String type,
    String status,
    Long groupId,
    Long assignedToId
) {}
