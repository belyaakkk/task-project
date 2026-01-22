package com.belyak.taskproject.infrastructure.persistence.repository;

import com.belyak.taskproject.domain.model.TaskStatus;
import com.belyak.taskproject.domain.model.TaskSummary;
import com.belyak.taskproject.domain.repository.TaskRepository;
import com.belyak.taskproject.infrastructure.persistence.mapper.TaskPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TaskRepositoryImpl implements TaskRepository {

    private final SpringDataTaskRepository springDataTaskRepository;
    private final TaskPersistenceMapper taskPersistenceMapper;

    @Override
    public List<TaskSummary> findAllByTeamIdAndStatus(UUID teamId, TaskStatus status) {
        return springDataTaskRepository.findAllByTeamIdAndStatus(teamId, status).stream()
                .map(taskPersistenceMapper::toSummary)
                .toList();
    }

    @Override
    public boolean existsByCategoryId(UUID categoryId) {
        return springDataTaskRepository.existsByCategoryId(categoryId);
    }

    @Override
    public boolean existsByTagId(UUID tagId) {
        return springDataTaskRepository.existsByTagsId(tagId);
    }
}
