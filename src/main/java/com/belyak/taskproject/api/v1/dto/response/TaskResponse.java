package com.belyak.taskproject.api.v1.dto.response;

import com.belyak.taskproject.api.v1.dto.info.CategoryInfo;
import com.belyak.taskproject.api.v1.dto.info.TagInfo;
import com.belyak.taskproject.api.v1.dto.info.UserInfo;
import com.belyak.taskproject.domain.model.TaskPriority;
import com.belyak.taskproject.domain.model.TaskStatus;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        Instant createdAt,
        Instant dueDate,
        CategoryInfo category,
        Set<TagInfo> tags,
        UserInfo assignee
) {
}
