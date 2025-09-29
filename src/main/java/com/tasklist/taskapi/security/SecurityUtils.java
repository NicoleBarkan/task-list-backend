package com.tasklist.taskapi.security;

import com.tasklist.taskapi.model.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class SecurityUtils {
  private SecurityUtils() {}

  public static Set<SimpleGrantedAuthority> toAuthorities(Collection<Role> roles) {
    return roles.stream()
        .map(r -> r.name().startsWith("ROLE_") ? r.name() : "ROLE_" + r.name())
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toSet());
  }

  public static Set<String> toRoleNames(Collection<Role> roles) {
    return roles.stream()
        .map(r -> r.name().startsWith("ROLE_") ? r.name() : "ROLE_" + r.name())
        .collect(Collectors.toSet());
  }

  public static boolean hasAnyRole(Authentication auth, String... roles) {
    if (auth == null || auth.getAuthorities() == null) return false;
    var set = auth.getAuthorities().stream()
        .map(a -> a.getAuthority())
        .collect(Collectors.toSet());
    for (String r : roles) {
      String full = r.startsWith("ROLE_") ? r : "ROLE_" + r;
      if (set.contains(full)) return true;
    }
    return false;
  }

  public static String stripRolePrefix(String role) {
    return role == null ? null : role.replaceFirst("^ROLE_", "");
  }

  public static List<String> roleNames(Authentication auth, boolean stripPrefix) {
    if (auth == null || auth.getAuthorities() == null) return List.of();
    var names = auth.getAuthorities().stream()
        .map(a -> a.getAuthority())
        .toList();
    if (!stripPrefix) return names;
    return names.stream().map(SecurityUtils::stripRolePrefix).toList();
  }
}
