package com.belyak.taskproject.infrastructure.policy;

import com.belyak.taskproject.domain.exception.TagDeletionException;
import com.belyak.taskproject.domain.model.Tag;
import com.belyak.taskproject.domain.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TagHasNoTasksRule implements TagDeletionRule {

    private final TaskRepository taskRepository;

    @Override
    public void validate(Tag tag) {
        if (taskRepository.existsByTagId(tag.getId())) {
            throw new TagDeletionException("Tag cannot be deleted because it has tasks");
        }
    }
}
