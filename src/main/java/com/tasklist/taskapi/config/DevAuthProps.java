package com.tasklist.taskapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "dev.auth")
public class DevAuthProps {
  private boolean enabled = false;
  private String username = "dev-admin";
  private List<String> roles = List.of("ROLE_ADMIN","ROLE_MANAGER","ROLE_USER");
 
  public boolean isEnabled() { return enabled; }
  public void setEnabled(boolean enabled) { this.enabled = enabled; }
  public String getUsername() { return username; }
  public void setUsername(String username) { this.username = username; }
  public List<String> getRoles() { return roles; }
  public void setRoles(List<String> roles) { this.roles = roles; }
}
