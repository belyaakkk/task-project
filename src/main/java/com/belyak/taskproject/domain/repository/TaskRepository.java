package com.belyak.taskproject.domain.repository;

import com.belyak.taskproject.domain.model.Task;
import com.belyak.taskproject.domain.model.TaskStatus;
import com.belyak.taskproject.domain.model.TaskSummary;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository {

    // --- WRITE ---
    Task save(Task task);

    Optional<Task> findById(UUID taskId);

    void deleteById(UUID taskId);

    // --- READ: PROJECTIONS ---

    List<TaskSummary> findAllByTeamIdAndStatus(UUID teamId, TaskStatus status);

    Optional<TaskSummary> findDetailsById(UUID taskId);

    // --- CHECKS & VALIDATION ---

    boolean existsByCategoryId(UUID categoryId);

    boolean existsByTagId(UUID tagId);

    boolean existsById(UUID taskId);
}
