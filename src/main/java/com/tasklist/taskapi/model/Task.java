package com.tasklist.taskapi.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Task {

    public static final long UNASSIGNED = -1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    private TaskType type;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    private LocalDateTime createdOn;

    private LocalDateTime updatedOn;

    @Column(name = "assigned_to")
    private Long assignedTo = UNASSIGNED;

    public Task() {}

    public Task(String title, String description, TaskType type, TaskStatus status, LocalDateTime createdOn) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.status = status;
        this.createdOn = createdOn;
        this.assignedTo = UNASSIGNED;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskType getType() {
        return type;
    }

    public void setType(TaskType type) {
        this.type = type;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public LocalDateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(LocalDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }

    public Long getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(Long assignedTo) {
        this.assignedTo = assignedTo;
    }
}
