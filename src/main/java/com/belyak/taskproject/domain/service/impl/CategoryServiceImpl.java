package com.belyak.taskproject.domain.service.impl;

import com.belyak.taskproject.domain.exception.CategoryAlreadyExistsException;
import com.belyak.taskproject.domain.model.Category;
import com.belyak.taskproject.domain.model.CategorySummary;
import com.belyak.taskproject.domain.model.TaskStatus;
import com.belyak.taskproject.domain.repository.CategoryRepository;
import com.belyak.taskproject.domain.service.CategoryService;
import com.belyak.taskproject.infrastructure.policy.CategoryDeletionRule;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final List<CategoryDeletionRule> deletionRules;

    @Override
    @Transactional(readOnly = true)
    public List<CategorySummary> findTeamCategories(UUID teamId) {
        return categoryRepository.findAllByTeamId(teamId, TaskStatus.PUBLISHED);
    }

    @Override
    @Transactional
    public Category createCategory(UUID teamId, String name) {
        if (categoryRepository.existsByName(teamId, name)) {
            throw new CategoryAlreadyExistsException(
                    "Category with name '%s' already exists.".formatted(name));
        }
        return categoryRepository.createCategory(teamId, name);
    }

    @Override
    @Transactional
    public void deleteCategory(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category with id '%s' not found".formatted(categoryId)));

        deletionRules.forEach(rule -> rule.validate(category));

        categoryRepository.deleteById(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public Category findCategoryById(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));
    }
}
