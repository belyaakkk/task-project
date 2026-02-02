package com.belyak.taskproject.infrastructure.policy;

import com.belyak.taskproject.domain.exception.CategoryDeletionException;
import com.belyak.taskproject.domain.model.Category;
import com.belyak.taskproject.domain.policy.CategoryDeletionRule;
import com.belyak.taskproject.domain.port.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryHasNoTasksRule implements CategoryDeletionRule {

    private final TaskRepository taskRepository;

    @Override
    public void validate(Category category) {
        if (taskRepository.existsByCategoryId(category.getId())) {
            throw new CategoryDeletionException("Category cannot be deleted because it has tasks");
        }
    }
}
