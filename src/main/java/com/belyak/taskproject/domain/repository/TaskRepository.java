package com.belyak.taskproject.domain.repository;

import com.belyak.taskproject.domain.model.TaskStatus;
import com.belyak.taskproject.domain.model.TaskSummary;

import java.util.List;
import java.util.UUID;

public interface TaskRepository {

    List<TaskSummary> findAllByTeamIdAndStatus(UUID teamId, TaskStatus status);

    boolean existsByCategoryId(UUID categoryId);

    boolean existsByTagId(UUID tagId);
}
