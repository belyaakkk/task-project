package com.belyak.taskproject.domain.port.repository;

import com.belyak.taskproject.domain.model.Task;
import com.belyak.taskproject.domain.model.TaskStatus;
import com.belyak.taskproject.infrastructure.persistence.projections.TaskInfoProjection;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository {

    // --- WRITE ---
    Task save(Task task);

    void deleteById(UUID taskId);

    // --- READ: DOMAIN ---

    Optional<Task> findById(UUID taskId);

    // --- READ: PROJECTIONS ---

    List<TaskInfoProjection> findAllByTeamIdAndStatus(UUID teamId, TaskStatus status);

    Optional<TaskInfoProjection> findDetailsById(UUID taskId);

    // --- CHECKS & VALIDATION ---

    boolean existsByCategoryId(UUID categoryId);

    boolean existsByTagId(UUID tagId);

    boolean existsById(UUID taskId);
}
