package com.belyak.taskproject.web.dto.response;

import com.belyak.taskproject.domain.model.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
@Schema(description = "Response after creating a task")
public record CreateTaskResponse(

        @Schema(description = "Unique identifier of the created task", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "Task title", example = "Fix login bug")
        String title,

        @Schema(description = "Task status", example = "TODO")
        TaskStatus status,

        @Schema(description = "Assigned user ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID assigneeId,

        @Schema(description = "Category ID", example = "d94e7731-9706-4074-8854-845722334455")
        UUID categoryId,

        @Schema(description = "Creation timestamp")
        Instant createdAt
) {
}
