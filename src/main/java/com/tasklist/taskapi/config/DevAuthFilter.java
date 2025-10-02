package com.tasklist.taskapi.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.tasklist.taskapi.model.Role;

import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import java.io.IOException;

@Component
@Profile("dev")
@Order(0)
@EnableConfigurationProperties(DevAuthProps.class)
public class DevAuthFilter extends OncePerRequestFilter {

  private final DevAuthProps props;

  public DevAuthFilter(DevAuthProps props) {
    this.props = props;
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {

    var ctx = SecurityContextHolder.getContext();
    var currentAuth = ctx.getAuthentication();

    String authz = request.getHeader("Authorization");
    boolean hasAuthHeader = authz != null && !authz.isBlank();
    if (currentAuth != null && currentAuth.isAuthenticated() || hasAuthHeader) {
      filterChain.doFilter(request, response);
      return;
    }

    if (props.isEnabled()) {
      String headerUser = request.getHeader("X-Dev-User");
      String headerRoles = request.getHeader("X-Dev-Roles");

      String username = (headerUser != null && !headerUser.isBlank()) ? headerUser : props.getUsername();

      var roles = (headerRoles != null && !headerRoles.isBlank())
          ? java.util.Arrays.stream(headerRoles.split(",")).map(String::trim).toList()
          : props.getRoles();

      var authorities = com.tasklist.taskapi.security.SecurityUtils.toAuthorities(
          roles.stream().map(Role::valueOf).toList()
      );

      var token = new UsernamePasswordAuthenticationToken(username, null, authorities);
      token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      ctx.setAuthentication(token);
    }

    filterChain.doFilter(request, response);
  }
}
