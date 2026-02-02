package com.belyak.taskproject.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.UUID;

@Builder
@Schema(description = "Team summary with membership information")
public record TeamSummaryResponse(

        @Schema(description = "Unique identifier of the team", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "Name of the team", example = "Development Team")
        String name,

        @Schema(description = "Whether the current user is the owner of the team")
        boolean isOwner,

        @Schema(description = "Number of members in the team", example = "5")
        int memberCount
) {
}
