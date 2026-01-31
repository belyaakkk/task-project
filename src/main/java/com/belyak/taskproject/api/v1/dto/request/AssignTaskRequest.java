package com.belyak.taskproject.api.v1.dto.request;

import java.util.UUID;

public record AssignTaskRequest(UUID assigneeId) {
}