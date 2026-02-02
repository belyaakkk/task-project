package com.belyak.taskproject.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Task {

    private final UUID id;
    private final UUID teamId;
    private final String title;
    private final String description;
    private final TaskStatus status;
    private final TaskPriority priority;
    private final Instant dueDate;
    private final Instant createdAt;

    private final UUID assigneeId;
    private final UUID categoryId;
    private final Set<UUID> tagIds;

    public static Task createNew(UUID teamId, String title, String description,
                                 TaskStatus status, TaskPriority priority, Instant dueDate,
                                 UUID categoryId, UUID assigneeId, Set<UUID> tagIds) {
        validateTitle(title);


        return Task.builder()
                .id(null)
                .teamId(teamId)
                .title(title.trim())
                .description(description != null ? description.trim() : "")
                .status(status != null ? status : TaskStatus.DRAFT)
                .priority(priority != null ? priority : TaskPriority.MEDIUM)
                .dueDate(dueDate)
                .createdAt(Instant.now())
                .assigneeId(assigneeId)
                .categoryId(categoryId)
                .tagIds(tagIds != null ? new HashSet<>(tagIds) : new HashSet<>())
                .build();
    }

    public Task updateDetails(String title, String description, TaskPriority priority,
                              Instant dueDate, UUID assigneeId, UUID categoryId, Set<UUID> tagIds) {
        validateTitle(title);

        return this.toBuilder()
                .title(title.trim())
                .description(description != null ? description.trim() : "")
                .priority(priority)
                .dueDate(dueDate)
                .assigneeId(assigneeId)
                .categoryId(categoryId)
                .tagIds(tagIds != null ? new HashSet<>(tagIds) : new HashSet<>())
                .build();
    }

    public Task changeStatus(TaskStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        return this.toBuilder().status(newStatus).build();
    }

    public Task assign(UUID newAssigneeId) {
        return this.toBuilder().assigneeId(newAssigneeId).build();
    }

    public Task unassign() {
        return this.toBuilder().assigneeId(null).build();
    }

    private static void validateTitle(String title) {
        if (title == null || title.isBlank() || title.length() < 3) {
            throw new IllegalArgumentException("Task title must be at least 3 characters");
        }
    }
}
