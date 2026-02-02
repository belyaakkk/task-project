package com.belyak.taskproject.web.dto.response;

import com.belyak.taskproject.domain.model.TaskPriority;
import com.belyak.taskproject.domain.model.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Builder
@Schema(description = "Task information with related entities")
public record TaskResponse(

        @Schema(description = "Unique identifier of the task", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "Task title", example = "Fix login bug")
        String title,

        @Schema(description = "Task description", example = "The login button is not responding...")
        String description,

        @Schema(description = "Task status", example = "IN_PROGRESS")
        TaskStatus status,

        @Schema(description = "Task priority", example = "HIGH")
        TaskPriority priority,

        @Schema(description = "Creation timestamp")
        Instant createdAt,

        @Schema(description = "Last update timestamp")
        Instant updatedAt,

        @Schema(description = "Due date")
        Instant dueDate,

        @Schema(description = "Category information")
        CategoryResponse category,

        @Schema(description = "Set of tags")
        Set<TagResponse> tags,

        @Schema(description = "Assigned user information")
        UserResponse assignee
) {
}