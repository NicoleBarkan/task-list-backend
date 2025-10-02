package com.tasklist.taskapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class GroupDto {
    public Long id;

    @NotBlank(message = "Group name must not be blank")
    @Size(max = 255, message = "Group name too long")
    public String name;

    @Size(max = 2000, message = "Description too long")
    public String description;
}
