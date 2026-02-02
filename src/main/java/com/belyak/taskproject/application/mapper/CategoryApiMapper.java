package com.belyak.taskproject.application.mapper;

import com.belyak.taskproject.web.dto.response.CategoryResponse;
import com.belyak.taskproject.web.dto.response.CategoryWithTasksResponse;
import com.belyak.taskproject.domain.model.Category;
import com.belyak.taskproject.infrastructure.persistence.projections.CategoryInfoWithTaskCountProjection;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryApiMapper {
    CategoryWithTasksResponse toWithTasksResponse(CategoryInfoWithTaskCountProjection projection);

    List<CategoryWithTasksResponse> toWithTasksResponseList(List<CategoryInfoWithTaskCountProjection> projection);

    CategoryResponse toResponse(Category domain);
}
