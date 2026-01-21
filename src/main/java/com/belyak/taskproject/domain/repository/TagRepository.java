package com.belyak.taskproject.domain.repository;

import com.belyak.taskproject.domain.model.Tag;
import com.belyak.taskproject.domain.model.TagSummary;
import com.belyak.taskproject.domain.model.TaskStatus;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface TagRepository {
    List<TagSummary> findAllByTeamId(UUID teamId, TaskStatus status);

    List<Tag> createTags(UUID teamId, List<Tag> tags);

    boolean canAccess(UUID tagId, UUID userId);

    void deleteById(UUID tagId);

    Set<String> findTagNamesByTeamId(UUID teamId);

    Optional<Tag> findById(UUID tagId);
}
