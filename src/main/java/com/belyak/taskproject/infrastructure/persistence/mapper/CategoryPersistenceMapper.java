package com.belyak.taskproject.infrastructure.persistence.mapper;

import com.belyak.taskproject.domain.model.Category;
import com.belyak.taskproject.infrastructure.persistence.entity.CategoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryPersistenceMapper {
    Category toDomain(CategoryEntity entity);
}