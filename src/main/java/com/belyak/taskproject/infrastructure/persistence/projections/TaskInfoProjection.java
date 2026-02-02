package com.belyak.taskproject.infrastructure.persistence.projections;

import com.belyak.taskproject.domain.model.TaskPriority;
import com.belyak.taskproject.domain.model.TaskStatus;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public interface TaskInfoProjection {
    UUID getId();
    String getTitle();
    String getDescription();
    TaskStatus getStatus();
    TaskPriority getPriority();
    Instant getCreatedAt();
    Instant getUpdatedAt();
    Instant getDueDate();
    CategoryInfoProjection getCategory();
    Set<TagInfoProjection> getTags();
    UserInfoProjection getAssignee();
}
