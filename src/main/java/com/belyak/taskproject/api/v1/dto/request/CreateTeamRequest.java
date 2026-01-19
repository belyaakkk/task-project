package com.belyak.taskproject.api.v1.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CreateTeamRequest(

        @NotBlank(message = "Team name is required")
        @Size(min = 3, max = 50, message = "Name must be between 3 and 50 chars")
        String name,

        @NotBlank(message = "Password is required")
        @Size(min = 4, max = 20, message = "Password must be between 4 and 20 chars")
        String password
) {
}
