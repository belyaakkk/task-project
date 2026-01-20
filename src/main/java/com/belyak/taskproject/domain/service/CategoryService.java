package com.belyak.taskproject.domain.service;

import com.belyak.taskproject.domain.model.Category;
import com.belyak.taskproject.domain.model.CategorySummary;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    List<CategorySummary> findTeamCategories(UUID teamId);

    Category createCategory(UUID teamId, String name);

    void deleteCategory(UUID categoryId);

    Category findCategoryById(UUID categoryId);
}
