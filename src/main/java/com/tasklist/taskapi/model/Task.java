package com.tasklist.taskapi.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    private TaskType type;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    private Instant createdOn;
    private Instant updatedOn;

    @PrePersist
    void onCreate() {
        if (this.createdOn == null) this.createdOn = Instant.now();
    }

    @PreUpdate
    void onUpdate() { this.updatedOn = Instant.now(); }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    protected Task() {}

    public Task(String title, String description, TaskType type, TaskStatus status, Group group) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.status = status;
        this.group = group;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TaskType getType() { return type; }
    public void setType(TaskType type) { this.type = type; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public Instant getCreatedOn() { return createdOn; }
    public void setCreatedOn(Instant createdOn) { this.createdOn = createdOn; }

    public Instant getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(Instant updatedOn) { this.updatedOn = updatedOn; }

    public User getAssignedTo() { return assignedTo; }
    public void setAssignedTo(User assignedTo) { this.assignedTo = assignedTo; }

    public Group getGroup() { return group; }
    public void setGroup(Group group) { this.group = group; }
}
