package com.belyak.taskproject.domain.service;

import com.belyak.taskproject.domain.model.Category;
import com.belyak.taskproject.domain.model.CategorySummary;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    List<CategorySummary> getAllCategories();

    Category createCategory(Category category);

    void deleteCategory(UUID categoryId);

    Category findCategoryById(UUID categoryId);
}
