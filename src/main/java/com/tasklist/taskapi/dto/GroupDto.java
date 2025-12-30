package com.tasklist.taskapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class GroupDto {
    public Long id;

    @NotBlank
    @Size(max = 50)
    public String title;

    @Size(max = 2000)
    public String description;
}
