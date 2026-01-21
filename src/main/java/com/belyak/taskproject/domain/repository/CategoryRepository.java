package com.belyak.taskproject.domain.repository;

import com.belyak.taskproject.domain.model.Category;
import com.belyak.taskproject.domain.model.CategorySummary;
import com.belyak.taskproject.domain.model.TaskStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {

    List<CategorySummary> findAllByTeamId(UUID teamId, TaskStatus status);

    Category createCategory(UUID teamId, String name);

    boolean existsByName(UUID teamId, String name);

    Optional<Category> findById(UUID categoryId);

    void deleteById(UUID categoryId);

    boolean existsById(UUID categoryId);

    boolean canAccess(UUID categoryId, UUID userId);
}
