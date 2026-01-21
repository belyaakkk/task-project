package com.belyak.taskproject.infrastructure.persistence.repository;

import com.belyak.taskproject.domain.model.Tag;
import com.belyak.taskproject.domain.model.TagSummary;
import com.belyak.taskproject.domain.model.TaskStatus;
import com.belyak.taskproject.domain.repository.TagRepository;
import com.belyak.taskproject.infrastructure.persistence.entity.TagEntity;
import com.belyak.taskproject.infrastructure.persistence.entity.TeamEntity;
import com.belyak.taskproject.infrastructure.persistence.mapper.TagPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TagRepositoryImpl implements TagRepository {

    private final SpringDataTagRepository springDataTagRepository;
    private final SpringDataTeamRepository springDataTeamRepository;
    private final TagPersistenceMapper tagPersistenceMapper;

    @Override
    @Transactional(readOnly = true)
    public List<TagSummary> findAllByTeamId(UUID teamId, TaskStatus status) {
        return springDataTagRepository.findTagsByTeamIdAndStatus(teamId, status).stream()
                .map(tagPersistenceMapper::toSummary)
                .toList();
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
    public Set<String> findTagNamesByTeamId(UUID teamId) {
        return springDataTagRepository.findTagsNameByTeamId(teamId);
    }

    @Override
    public Optional<Tag> findById(UUID tagId) {
        return springDataTagRepository.findById(tagId).map(tagPersistenceMapper::toDomain);
    }
}
