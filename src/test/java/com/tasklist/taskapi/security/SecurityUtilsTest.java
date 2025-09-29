package com.tasklist.taskapi.security;

import com.tasklist.taskapi.model.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SecurityUtilsTest {

  @Test
  void toAuthorities_addsPrefixIfMissing() {
    var out = SecurityUtils.toAuthorities(List.of(Role.USER, Role.ADMIN));
    assertThat(out).contains(
        new SimpleGrantedAuthority("ROLE_USER"),
        new SimpleGrantedAuthority("ROLE_ADMIN")
    );
  }

  @Test
  void hasAnyRole_detectsRoleProperly() {
    var auth = new UsernamePasswordAuthenticationToken(
        "vasya", "N/A",
        List.of(new SimpleGrantedAuthority("ROLE_USER"))
    );
    assertThat(SecurityUtils.hasAnyRole(auth, "USER")).isTrue();
    assertThat(SecurityUtils.hasAnyRole(auth, "ADMIN")).isFalse();
  }
}
