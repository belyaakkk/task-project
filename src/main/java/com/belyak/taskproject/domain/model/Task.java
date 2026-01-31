package com.belyak.taskproject.domain.model;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Builder
@Data
public class Task {
    private final UUID id;
    private UUID teamId;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;

    private UUID assignee;
    private UUID categoryId;
    Set<UUID> tagIds;

    private Instant dueDate;
    private Instant createdAt;
}
