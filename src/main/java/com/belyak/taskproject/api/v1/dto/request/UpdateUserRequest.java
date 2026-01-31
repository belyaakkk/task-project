package com.belyak.taskproject.api.v1.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UpdateUserRequest(
        @NotBlank(message = "Name is required.")
        @Size(min = 1, max = 50, message = "Name must be between 2 and 50 characters")
        String name
) {
}
