package com.belyak.taskproject.domain.service;

import com.belyak.taskproject.api.v1.dto.request.CreateTagsRequest;
import com.belyak.taskproject.domain.model.Tag;
import com.belyak.taskproject.domain.model.TagSummaryWithTaskCount;

import java.util.List;
import java.util.UUID;

public interface TagService {
    List<TagSummaryWithTaskCount> findTeamTags(UUID teamId);

    List<Tag> createTags(UUID teamId, CreateTagsRequest request);

    void deleteTag(UUID tagId);
}
