package com.belyak.taskproject.infrastructure.persistence.mapper;

import com.belyak.taskproject.domain.model.Tag;
import com.belyak.taskproject.domain.model.TagSummaryWithTaskCount;
import com.belyak.taskproject.infrastructure.persistence.entity.TagEntity;
import com.belyak.taskproject.infrastructure.persistence.projections.TagInfoWithTaskCountProjection;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TagPersistenceMapper {
    TagSummaryWithTaskCount toSummary(TagInfoWithTaskCountProjection projection);

    Tag toDomain(TagEntity entity);

    TagEntity toEntity(Tag domain);
}
