package com.belyak.taskproject.infrastructure.policy;

import com.belyak.taskproject.domain.exception.CategoryDeletionException;
import com.belyak.taskproject.domain.model.Category;
import com.belyak.taskproject.domain.policy.CategoryDeletionRule;
import org.springframework.stereotype.Component;

@Component
public class SystemCategoryProtectionRule implements CategoryDeletionRule {

    @Override
    public void validate(Category category) {
        if (category.isSystem()) {
            throw new CategoryDeletionException("Cannot delete system category '%s'".formatted(category.getName()));
        }
    }
}
