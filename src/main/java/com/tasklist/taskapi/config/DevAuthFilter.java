package com.tasklist.taskapi.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Profile("dev")
@Order(0)
public class DevAuthFilter extends OncePerRequestFilter {
  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {

    var ctx = SecurityContextHolder.getContext();
    var auth = ctx.getAuthentication();

    String authz = request.getHeader("Authorization");
    boolean hasAuthHeader = authz != null && !authz.isBlank();

    if ((auth == null || !auth.isAuthenticated()
         || "anonymousUser".equals(String.valueOf(auth.getPrincipal())))
        && !hasAuthHeader) {

      var token = new UsernamePasswordAuthenticationToken(
          "dev-admin", "N/A",
          List.of(new SimpleGrantedAuthority("ROLE_ADMIN"),
                  new SimpleGrantedAuthority("ROLE_MANAGER"),
                  new SimpleGrantedAuthority("ROLE_USER"))
      );
      ctx.setAuthentication(token);
    }

    filterChain.doFilter(request, response);
  }
}
