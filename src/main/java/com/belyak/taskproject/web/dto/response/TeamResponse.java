package com.belyak.taskproject.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.UUID;

@Builder
@Schema(description = "Basic team information")
public record TeamResponse(

        @Schema(description = "Unique identifier of the team", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "Name of the team", example = "Development Team")
        String name,

        @Schema(description = "Join code for the team", example = "ABC123")
        String joinCode
) {
}
