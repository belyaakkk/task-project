package com.belyak.taskproject.infrastructure.persistence.mapper;

import com.belyak.taskproject.domain.model.Category;
import com.belyak.taskproject.domain.model.CategorySummary;
import com.belyak.taskproject.infrastructure.persistence.entity.CategoryEntity;
import com.belyak.taskproject.infrastructure.persistence.projections.CategorySummaryProjection;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryPersistenceMapper {

    CategorySummary toSummary(CategorySummaryProjection projection);

    Category toDomain(CategoryEntity entity);

    CategoryEntity toEntity(Category category);
}
