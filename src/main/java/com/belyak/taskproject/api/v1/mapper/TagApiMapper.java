package com.belyak.taskproject.api.v1.mapper;

import com.belyak.taskproject.api.v1.dto.response.CreateTagResponse;
import com.belyak.taskproject.api.v1.dto.response.TagResponse;
import com.belyak.taskproject.domain.model.Tag;
import com.belyak.taskproject.domain.model.TagSummary;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TagApiMapper {
    TagResponse toResponse(TagSummary summary);

    List<TagResponse> toResponseList(List<TagSummary> summary);

    CreateTagResponse toCreateResponse(Tag tag);

    List<CreateTagResponse> toCreateResponseList(List<Tag> tags);
}
