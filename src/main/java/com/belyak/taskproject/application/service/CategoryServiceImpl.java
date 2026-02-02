package com.belyak.taskproject.application.service;

import com.belyak.taskproject.domain.exception.CategoryAlreadyExistsException;
import com.belyak.taskproject.domain.model.Category;
import com.belyak.taskproject.domain.model.TaskStatus;
import com.belyak.taskproject.domain.port.repository.CategoryRepository;
import com.belyak.taskproject.domain.port.service.CategoryService;
import com.belyak.taskproject.infrastructure.persistence.projections.CategoryInfoWithTaskCountProjection;
import com.belyak.taskproject.domain.policy.CategoryDeletionRule;
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
    public List<CategoryInfoWithTaskCountProjection> findTeamCategories(UUID teamId) {
        return categoryRepository.findAllByTeamId(teamId, TaskStatus.IN_PROGRESS);
    }

    @Override
    @Transactional
    public Category createCategory(UUID teamId, String name) {
        if (categoryRepository.existsByName(teamId, name)) {
            throw new CategoryAlreadyExistsException(
                    "Category with name '%s' already exists.".formatted(name));
        }
        Category newCategory = Category.createNew(name);
        return categoryRepository.createCategory(teamId, newCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category with id '%s' not found".formatted(categoryId)));

        deletionRules.forEach(rule -> rule.validate(category));

        categoryRepository.deleteById(categoryId);
    }
}
