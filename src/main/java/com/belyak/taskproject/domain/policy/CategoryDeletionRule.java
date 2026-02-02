package com.belyak.taskproject.domain.policy;

import com.belyak.taskproject.domain.model.Category;

public interface CategoryDeletionRule {
    void validate(Category category);
}
