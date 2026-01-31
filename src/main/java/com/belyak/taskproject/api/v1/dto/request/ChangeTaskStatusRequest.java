package com.belyak.taskproject.api.v1.dto.request;

import com.belyak.taskproject.domain.model.TaskStatus;

public record ChangeTaskStatusRequest(TaskStatus status) {}