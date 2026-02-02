package com.belyak.taskproject.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@Schema(description = "Request to create a new team")
public record CreateTeamRequest(

        @Schema(description = "Team name", example = "Development Team", minLength = 3, maxLength = 50)
        @NotBlank(message = "Team name is required")
        @Size(min = 3, max = 50, message = "Name must be between {min} and {max} characters")
        String name,

        @Schema(description = "Team password for joining", example = "secret123", minLength = 4, maxLength = 20)
        @NotBlank(message = "Password is required")
        @Size(min = 4, max = 20, message = "Password must be between {min} and {max} characters")
        String password
) {
}
