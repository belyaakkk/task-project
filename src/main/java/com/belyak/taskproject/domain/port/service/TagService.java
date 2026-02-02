package com.belyak.taskproject.domain.port.service;

import com.belyak.taskproject.web.dto.request.CreateTagsRequest;
import com.belyak.taskproject.domain.model.Tag;
import com.belyak.taskproject.infrastructure.persistence.projections.TagInfoWithTaskCountProjection;

import java.util.List;
import java.util.UUID;

public interface TagService {
    List<TagInfoWithTaskCountProjection> findTeamTags(UUID teamId);

    List<Tag> createTags(UUID teamId, CreateTagsRequest request);

    void deleteTag(UUID tagId);
}
