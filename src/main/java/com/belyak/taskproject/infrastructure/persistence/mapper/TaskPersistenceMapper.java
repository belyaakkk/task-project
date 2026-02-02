package com.belyak.taskproject.infrastructure.persistence.mapper;

import com.belyak.taskproject.domain.model.Task;
import com.belyak.taskproject.infrastructure.persistence.entity.TagEntity;
import com.belyak.taskproject.infrastructure.persistence.entity.TaskEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaskPersistenceMapper {

    @Mapping(target = "assigneeId", source = "assignee.id")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "teamId", source = "team.id")
    @Mapping(target = "tagIds", source = "tags", qualifiedByName = "mapTagsToIds")
    Task toDomain(TaskEntity entity);

    @Named("mapTagsToIds")
    default Set<UUID> mapTagsToIds(Set<TagEntity> tags) {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptySet();
        }
        return tags.stream()
                .map(TagEntity::getId)
                .collect(Collectors.toSet());
    }
}
