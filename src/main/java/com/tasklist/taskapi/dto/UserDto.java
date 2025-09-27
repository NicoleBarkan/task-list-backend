package com.tasklist.taskapi.dto;

import com.tasklist.taskapi.model.Role;
import com.tasklist.taskapi.model.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserDto(
        Long id,
        String username,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String password,
        String firstName,           
        String lastName,        
        Set<String> role,
        Long groupId
) {
    public static UserDto from(User u) {
        return new UserDto(
                u.getId(),
                u.getUsername(),
                null, 
                u.getFirstName(),                         
                u.getLastName(),                     
                u.getRole() != null
                        ? u.getRole().stream().map(Role::name).collect(Collectors.toSet())
                        : Set.of(),
                u.getGroup() != null ? u.getGroup().getId() : null
        );
    }
}
