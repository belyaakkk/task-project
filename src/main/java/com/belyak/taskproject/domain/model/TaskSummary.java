package com.belyak.taskproject.domain.model;

import lombok.Builder;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Builder
public record TaskSummary(
        UUID id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        Instant createdAt,
        Instant updatedAt,
        Instant dueDate,
        CategorySummary category,
        Set<TagSummary> tags,
        UserSummary assignee
) {
}
