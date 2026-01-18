package com.belyak.taskproject.domain.listener;

import com.belyak.taskproject.domain.event.CategoryDeleteEvent;
import com.belyak.taskproject.domain.exception.CategoryDeletionException;
import com.belyak.taskproject.domain.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskCategoryEventListener {

    private final TaskRepository taskRepository;

    @EventListener
    public void onCategoryDeleteRequest(CategoryDeleteEvent event) {
        if (taskRepository.existsByCategoryId(event.categoryId())) {
            throw new CategoryDeletionException(
                    "Category cannot be deleted because it has tasks"
            );
        }
    }
}
