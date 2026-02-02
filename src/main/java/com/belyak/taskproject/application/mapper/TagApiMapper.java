package com.belyak.taskproject.application.mapper;

import com.belyak.taskproject.web.dto.response.TagResponse;
import com.belyak.taskproject.domain.model.Tag;
import com.belyak.taskproject.web.dto.response.TagWithTasksResponse;
import com.belyak.taskproject.infrastructure.persistence.projections.TagInfoWithTaskCountProjection;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TagApiMapper {
    TagWithTasksResponse toWithTasksResponse(TagInfoWithTaskCountProjection projection);

    List<TagWithTasksResponse> toWithTasksResponseList(List<TagInfoWithTaskCountProjection> projections);

    TagResponse toResponse(Tag domain);

    List<TagResponse> toResponseList(List<Tag> domains);
}
