package com.tasklist.taskapi.dto;

import java.time.LocalDateTime;

import com.tasklist.taskapi.model.Task;
import com.fasterxml.jackson.annotation.JsonProperty;

public record TaskDto(
    Long id,
    String title,
    String description,
    String type,
    String status,
    @JsonProperty("createdOn")  LocalDateTime createdOn,
    @JsonProperty("updatedOn")  LocalDateTime updatedOn,
    @JsonProperty("assignedTo") Long assignedTo,
    String assignedToName,
    Long groupId
) {
    public static TaskDto from(Task t) {
        return from(t, null);
    }

    public static TaskDto from(Task t, String assignedToName) {
        return new TaskDto(
            t.getId(),
            t.getTitle(),
            t.getDescription(),
            t.getType() != null ? t.getType().name() : null,
            t.getStatus() != null ? t.getStatus().name() : null,
            t.getCreatedOn(),
            t.getUpdatedOn(),
            t.getAssignedTo(),
            assignedToName,
            t.getGroup() != null ? t.getGroup().getId() : null
        );
    }
}
