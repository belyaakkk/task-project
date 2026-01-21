package com.belyak.taskproject.domain.service.impl;

import com.belyak.taskproject.api.v1.dto.request.CreateTagsRequest;
import com.belyak.taskproject.domain.exception.TagAlreadyExistsException;
import com.belyak.taskproject.domain.model.Tag;
import com.belyak.taskproject.domain.model.TagConstants;
import com.belyak.taskproject.domain.model.TagSummary;
import com.belyak.taskproject.domain.model.TaskStatus;
import com.belyak.taskproject.domain.repository.TagRepository;
import com.belyak.taskproject.domain.repository.TeamRepository;
import com.belyak.taskproject.domain.service.TagService;
import com.belyak.taskproject.infrastructure.policy.TagDeletionRule;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final TeamRepository teamRepository;

    private final List<TagDeletionRule> deletionRules;

    @Override
    @Transactional(readOnly = true)
    public List<TagSummary> findTeamTags(UUID teamId) {
        return tagRepository.findAllByTeamId(teamId, TaskStatus.PUBLISHED);
    }

    @Override
    @Transactional
    public List<Tag> createTags(UUID teamId, CreateTagsRequest request) {
        if (!teamRepository.existsById(teamId)) {
            throw new EntityNotFoundException("Team not found with id " + teamId);
        }

        Set<String> requestedNames = request.tags().stream()
                .map(CreateTagsRequest.TagItem::name)
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        if (requestedNames.size() != request.tags().size()) {
            throw new IllegalArgumentException("Duplicate tag names in request");
        }

        Set<String> existingNames = tagRepository.findTagNamesByTeamId(teamId);

        List<String> conflicts = requestedNames.stream()
                .filter(reqName -> existingNames.stream()
                        .anyMatch(exName -> exName.equalsIgnoreCase(reqName)))
                .toList();

        if (!conflicts.isEmpty()) {
            throw new TagAlreadyExistsException("Tags already exist: " + conflicts);
        }

        List<Tag> tagsToCreate = request.tags().stream()
                .map(tag -> Tag.builder()
                        .name(tag.name().trim())
                        .color(tag.color() != null ? tag.color() : TagConstants.DEFAULT_COLOR)
                        .build())
                .toList();

        return tagRepository.createTags(teamId, tagsToCreate);
    }

    @Override
    @Transactional
    public void deleteTag(UUID tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException("Tag with id '%s' not found".formatted(tagId)));

        deletionRules.forEach(rule -> rule.validate(tag));

        tagRepository.deleteById(tagId);
    }
}
