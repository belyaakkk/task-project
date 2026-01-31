package com.belyak.taskproject.api.v1.dto.request;

import com.belyak.taskproject.domain.model.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UpdateTaskRequest(
        @NotBlank @Size(min = 3, max = 100) String title,
        @NotBlank String description,
        TaskPriority priority,
        Instant dueDate,
        UUID categoryId,
        Set<UUID> tagIds
) {
}
