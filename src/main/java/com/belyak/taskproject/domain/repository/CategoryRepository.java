package com.belyak.taskproject.domain.repository;

import com.belyak.taskproject.domain.model.Category;
import com.belyak.taskproject.domain.model.CategorySummary;
import com.belyak.taskproject.domain.model.TaskStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {

    List<CategorySummary> findAllSummaries(TaskStatus status);

    Category save(Category category);

    boolean existsByNameIgnoreCase(String name);

    Optional<Category> findById(UUID categoryId);

    Optional<CategorySummary> findSummaryById(UUID categoryId, TaskStatus status);

    void deleteById(UUID categoryId);

    boolean existsById(UUID categoryId);
}
