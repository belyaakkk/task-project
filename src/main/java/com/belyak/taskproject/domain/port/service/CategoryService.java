package com.belyak.taskproject.domain.port.service;

import com.belyak.taskproject.domain.model.Category;
import com.belyak.taskproject.infrastructure.persistence.projections.CategoryInfoWithTaskCountProjection;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    List<CategoryInfoWithTaskCountProjection> findTeamCategories(UUID teamId);

    Category createCategory(UUID teamId, String name);

    void deleteCategory(UUID categoryId);
}
