package com.belyak.taskproject.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@Schema(description = "Request to join an existing team")
public record JoinTeamRequest(

        @Schema(description = "Team join code", example = "ABC123")
        @NotBlank(message = "Join code is required")
        String joinCode,

        @Schema(description = "Team password", example = "secret123")
        @NotBlank(message = "Password is required")
        String password
) {
}
