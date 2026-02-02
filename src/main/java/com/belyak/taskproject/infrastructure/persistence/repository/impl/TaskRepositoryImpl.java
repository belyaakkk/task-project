package com.belyak.taskproject.infrastructure.persistence.repository.impl;

import com.belyak.taskproject.domain.model.Task;
import com.belyak.taskproject.domain.model.TaskStatus;
import com.belyak.taskproject.domain.port.repository.TaskRepository;
import com.belyak.taskproject.infrastructure.persistence.entity.TagEntity;
import com.belyak.taskproject.infrastructure.persistence.entity.TaskEntity;
import com.belyak.taskproject.infrastructure.persistence.mapper.TaskPersistenceMapper;
import com.belyak.taskproject.infrastructure.persistence.projections.TaskInfoProjection;
import com.belyak.taskproject.infrastructure.persistence.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class TaskRepositoryImpl implements TaskRepository {

    private final SpringDataTaskRepository springDataTaskRepository;
    private final SpringDataTeamRepository teamRepository;
    private final SpringDataUserRepository userRepository;
    private final SpringDataCategoryRepository categoryRepository;
    private final SpringDataTagRepository tagRepository;
    private final TaskPersistenceMapper taskPersistenceMapper;

    @Override
    @Transactional
    public Task save(Task task) {
        TaskEntity entity;

        if (task.getId() != null) {
            entity = springDataTaskRepository.findById(task.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Task not found"));
        } else {
            entity = new TaskEntity();
            entity.setTeam(teamRepository.getReferenceById(task.getTeamId()));
        }

        entity.setTitle(task.getTitle());
        entity.setDescription(task.getDescription());
        entity.setStatus(task.getStatus());
        entity.setPriority(task.getPriority());
        entity.setDueDate(task.getDueDate());

        if (task.getCategoryId() != null) {
            entity.setCategory(categoryRepository.getReferenceById(task.getCategoryId()));
        }

        if (task.getAssigneeId() != null) {
            entity.setAssignee(userRepository.getReferenceById(task.getAssigneeId()));
        } else {
            entity.setAssignee(null);
        }

        if (task.getTagIds() != null && !task.getTagIds().isEmpty()) {
            Set<TagEntity> tagProxies = task.getTagIds().stream()
                    .map(tagRepository::getReferenceById)
                    .collect(Collectors.toSet());
            entity.setTags(tagProxies);
        } else {
            entity.getTags().clear();
        }

        return taskPersistenceMapper.toDomain(springDataTaskRepository.save(entity));
    }

    @Override
    public Optional<Task> findById(UUID taskId) {
        return springDataTaskRepository.findById(taskId)
                .map(taskPersistenceMapper::toDomain);
    }

    @Override
    @Transactional
    public void deleteById(UUID taskId) {
        springDataTaskRepository.deleteById(taskId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskInfoProjection> findAllByTeamIdAndStatus(UUID teamId, TaskStatus status) {
        return springDataTaskRepository.findAllByTeamIdAndStatus(teamId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TaskInfoProjection> findDetailsById(UUID taskId) {
        return springDataTaskRepository.findProjectedById(taskId);
    }


    @Override
    public boolean existsByCategoryId(UUID categoryId) {
        return springDataTaskRepository.existsByCategoryId(categoryId);
    }

    @Override
    public boolean existsByTagId(UUID tagId) {
        return springDataTaskRepository.existsByTagsId(tagId);
    }

    @Override
    public boolean existsById(UUID taskId) {
        return springDataTaskRepository.existsById(taskId);
    }
}
