package com.belyak.taskproject.api.v1.mapper;

import com.belyak.taskproject.api.v1.dto.response.CategoryResponse;
import com.belyak.taskproject.api.v1.dto.response.CreateCategoryResponse;
import com.belyak.taskproject.domain.model.Category;
import com.belyak.taskproject.domain.model.CategorySummary;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryApiMapper {
    CategoryResponse toResponse(CategorySummary summary);

    List<CategoryResponse> toResponseList(List<CategorySummary> summaries);

    CreateCategoryResponse toCreateResponse(Category category);
}
