package com.belyak.taskproject.infrastructure.policy;

import java.util.UUID;

public interface CategoryDeletionRule {
    void validate(UUID categoryId);
}
