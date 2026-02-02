package com.belyak.taskproject.domain.port.repository;

import com.belyak.taskproject.domain.model.Tag;
import com.belyak.taskproject.domain.model.TaskStatus;
import com.belyak.taskproject.infrastructure.persistence.projections.TagInfoWithTaskCountProjection;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface TagRepository {
    // --- WRITE ---

    List<Tag> createTags(UUID teamId, List<Tag> tags);

    void deleteById(UUID tagId);

    // --- READ: DOMAIN ---

    Optional<Tag> findById(UUID tagId);

    Set<String> findExistingTagNames(UUID teamId, Set<String> names);

    // --- READ: PROJECTIONS ---

    List<TagInfoWithTaskCountProjection> findAllByTeamId(UUID teamId, TaskStatus status);

    // --- CHECKS & VALIDATION ---

    boolean canAccess(UUID tagId, UUID userId);
}
