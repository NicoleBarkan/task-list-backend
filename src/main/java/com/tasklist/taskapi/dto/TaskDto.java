package com.tasklist.taskapi.dto;
import java.time.Instant;

import com.tasklist.taskapi.model.Task;

public record TaskDto(
    Long id,
    String title,
    String description,
    String type,
    String status,
    Instant createdOn,
    Instant updatedOn,
    Long assignedTo,
    String assignedToName,
    Long groupId
) {
    public static TaskDto from(Task t, String assignedToName) {
        return new TaskDto(
            t.getId(),
            t.getTitle(),
            t.getDescription(),
            t.getType() != null ? t.getType().name() : null,
            t.getStatus() != null ? t.getStatus().name() : null,
            t.getCreatedOn(),
            t.getUpdatedOn(),
            t.getAssignedTo() != null ? t.getAssignedTo().getId() : null,
            assignedToName,
            t.getGroup() != null ? t.getGroup().getId() : null
        );
    }
}
