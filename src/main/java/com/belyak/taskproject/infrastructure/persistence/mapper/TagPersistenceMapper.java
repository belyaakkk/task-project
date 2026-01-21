package com.belyak.taskproject.infrastructure.persistence.mapper;

import com.belyak.taskproject.domain.model.Tag;
import com.belyak.taskproject.domain.model.TagSummary;
import com.belyak.taskproject.infrastructure.persistence.entity.TagEntity;
import com.belyak.taskproject.infrastructure.persistence.projections.TagSummaryProjection;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TagPersistenceMapper {
    TagSummary toSummary(TagSummaryProjection projection);

    Tag toDomain(TagEntity entity);

    TagEntity toEntity(Tag domain);
}
