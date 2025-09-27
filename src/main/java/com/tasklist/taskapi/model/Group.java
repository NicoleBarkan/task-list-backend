package com.tasklist.taskapi.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_groups", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class Group {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)  
    private String name;

    @Column(length = 2000)                 
    private String description;

    @Column(nullable=false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable=false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void onUpdate() { this.updatedAt = Instant.now(); }
    
    public Long getId() { 
        return id; 
    }
    public void setId(Long id) { 
        this.id = id; 
    }

    public String getName() { 
        return name; 
    }
    public void setName(String name) { 
        this.name = name; 
    }

    public String getDescription() { 
        return description; 
    }
    public void setDescription(String description) { 
        this.description = description; 
    }

}
