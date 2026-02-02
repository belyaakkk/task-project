package com.belyak.taskproject.web.dto.response;

import com.belyak.taskproject.domain.model.TeamStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.Set;
import java.util.UUID;

@Builder
@Schema(description = "Detailed team information with members")
public record TeamDetailsResponse(

        @Schema(description = "Unique identifier of the team", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "Name of the team", example = "Development Team")
        String name,

        @Schema(description = "Team status", example = "ACTIVE")
        TeamStatus status,

        @Schema(description = "Team owner information")
        UserResponse owner,

        @Schema(description = "Set of team members")
        Set<UserResponse> members
) {
}