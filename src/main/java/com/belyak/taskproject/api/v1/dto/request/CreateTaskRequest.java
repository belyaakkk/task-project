package com.belyak.taskproject.api.v1.dto.request;

import com.belyak.taskproject.domain.model.TaskPriority;
import com.belyak.taskproject.domain.model.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Builder
public record CreateTaskRequest(

        @Schema(description = "Task title", example = "Fix login bug")
        @NotBlank(message = "Title is required")
        @Size(min = 3, max = 100, message = "Title must be between {min} and {max} characters")
        String title,

        @Schema(description = "Detailed description", example = "The login button is not responding...")
        @NotBlank(message = "Description is required")
        String description,

        @Schema(description = "Task status", example = "IN_PROGRESS", defaultValue = "TODO")
        TaskStatus status,

        @Schema(description = "Task priority", example = "HIGH", defaultValue = "MEDIUM")
        TaskPriority priority,

        @Schema(description = "Due date in ISO format", example = "2026-01-30T10:00:00Z")
        @FutureOrPresent(message = "Due date cannot be in the past")
        Instant dueDate,

        @Schema(description = "ID of the assigned user", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID assigneeId,

        @Schema(description = "ID of the category", example = "d94e7731-9706-4074-8854-845722334455")
        @NotNull(message = "Category ID is required")
        UUID categoryId,

        @Schema(description = "List of tag IDs")
        Set<UUID> tagIds
) {
}
