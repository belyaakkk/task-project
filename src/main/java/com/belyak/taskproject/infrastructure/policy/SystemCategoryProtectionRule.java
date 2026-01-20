package com.belyak.taskproject.infrastructure.policy;

import com.belyak.taskproject.domain.exception.CategoryDeletionException;
import com.belyak.taskproject.domain.model.Category;
import com.belyak.taskproject.domain.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SystemCategoryProtectionRule implements CategoryDeletionRule {

    private final CategoryRepository categoryRepository;

    @Override
    public void validate(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));

        if ("BACKLOG".equalsIgnoreCase(category.getName())) {
            throw new CategoryDeletionException("Cannot delete system category 'Backlog'");
        }
    }
}
