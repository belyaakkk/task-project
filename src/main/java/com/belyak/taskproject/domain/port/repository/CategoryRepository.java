package com.belyak.taskproject.domain.port.repository;

import com.belyak.taskproject.domain.model.Category;
import com.belyak.taskproject.domain.model.TaskStatus;
import com.belyak.taskproject.infrastructure.persistence.projections.CategoryInfoWithTaskCountProjection;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {
    // --- WRITE ---

    void deleteById(UUID categoryId);

    Category createCategory(UUID teamId, Category category);

    // --- READ: DOMAIN ---

    Optional<Category> findById(UUID categoryId);

    // --- READ: PROJECTIONS ---

    List<CategoryInfoWithTaskCountProjection> findAllByTeamId(UUID teamId, TaskStatus status);

    // --- CHECKS & VALIDATION ---

    boolean canAccess(UUID categoryId, UUID userId);

    boolean existsByName(UUID teamId, String name);
}
