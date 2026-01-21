package com.belyak.taskproject.infrastructure.persistence.repository;

import com.belyak.taskproject.domain.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TaskRepositoryImpl implements TaskRepository {

    private final SpringTaskPostRepository springDataPostRepository;

    @Override
    public boolean existsByCategoryId(UUID categoryId) {
        return springDataPostRepository.existsByCategoryId(categoryId);
    }

    @Override
    public boolean existsByTagId(UUID tagId) {
        return springDataPostRepository.existsByTagsId(tagId);
    }
}
