package com.tasklist.taskapi.model;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class RoleEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, unique = true)
  private Role name;

  public Long getId() { return id; }
  public Role getName() { return name; }
  public void setName(Role name) { this.name = name; }
}
