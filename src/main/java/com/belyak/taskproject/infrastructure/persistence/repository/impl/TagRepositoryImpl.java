package com.belyak.taskproject.infrastructure.persistence.repository.impl;

import com.belyak.taskproject.domain.model.Tag;
import com.belyak.taskproject.domain.model.TaskStatus;
import com.belyak.taskproject.domain.port.repository.TagRepository;
import com.belyak.taskproject.infrastructure.persistence.entity.TagEntity;
import com.belyak.taskproject.infrastructure.persistence.entity.TeamEntity;
import com.belyak.taskproject.infrastructure.persistence.mapper.TagPersistenceMapper;
import com.belyak.taskproject.infrastructure.persistence.projections.TagInfoWithTaskCountProjection;
import com.belyak.taskproject.infrastructure.persistence.repository.SpringDataTagRepository;
import com.belyak.taskproject.infrastructure.persistence.repository.SpringDataTeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class TagRepositoryImpl implements TagRepository {

    private final SpringDataTagRepository springDataTagRepository;
    private final SpringDataTeamRepository springDataTeamRepository;
    private final TagPersistenceMapper tagPersistenceMapper;

    @Override
    @Transactional(readOnly = true)
    public List<TagInfoWithTaskCountProjection> findAllByTeamId(UUID teamId, TaskStatus status) {
        return springDataTagRepository.findTagsByTeamIdAndStatus(teamId, status);
    }

    @Override
    @Transactional
    public List<Tag> createTags(UUID teamId, List<Tag> tags) {
        TeamEntity teamProxy = springDataTeamRepository.getReferenceById(teamId);

        List<TagEntity> entities = tags.stream()
                .map(domainTag -> {
                    TagEntity entity = tagPersistenceMapper.toEntity(domainTag);
                    entity.setTeam(teamProxy);
                    return entity;
                })
                .toList();

        List<TagEntity> savedEntities = springDataTagRepository.saveAll(entities);

        return savedEntities.stream()
                .map(tagPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean canAccess(UUID tagId, UUID userId) {
        return springDataTagRepository.canAccess(tagId, userId);
    }

    @Override
    public void deleteById(UUID tagId) {
        springDataTagRepository.deleteById(tagId);
    }

    @Override
    public Optional<Tag> findById(UUID tagId) {
        return springDataTagRepository.findById(tagId)
                .map(tagPersistenceMapper::toDomain);
    }

    @Override
    public Set<String> findExistingTagNames(UUID teamId, Set<String> names) {
        if (names.isEmpty()) return Collections.emptySet();

        Set<String> lowerCaseNames = names.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        return springDataTagRepository.findNamesByTeamIdAndNamesIn(teamId, lowerCaseNames);
    }
}
