package com.belyak.taskproject.infrastructure.persistence.mapper;

import com.belyak.taskproject.domain.model.Tag;
import com.belyak.taskproject.infrastructure.persistence.entity.TagEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TagPersistenceMapper {
    Tag toDomain(TagEntity entity);

    @Mapping(target = "team", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    TagEntity toEntity(Tag tag);
}