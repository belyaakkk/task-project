package com.belyak.taskproject.api.v1.dto.response;

import com.belyak.taskproject.domain.model.TaskStatus;

import java.time.Instant;
import java.util.UUID;

public record CreateTaskResponse(
        UUID id,
        String title,
        TaskStatus status,
        UUID assigneeId,
        UUID categoryId,
        Instant createdAt) {
}
