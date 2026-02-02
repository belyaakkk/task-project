package com.belyak.taskproject.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.UUID;

@Builder
@Schema(description = "Request to assign a task to a user")
public record AssignTaskRequest(

        @Schema(description = "ID of the user to assign the task to. Null to unassign.",
                example = "550e8400-e29b-41d4-a716-446655440000")
        UUID assigneeId
) {
}