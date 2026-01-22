package com.belyak.taskproject.api.v1.mapper;

import com.belyak.taskproject.api.v1.dto.response.CategoryResponse;
import com.belyak.taskproject.api.v1.dto.response.CreateCategoryResponse;
import com.belyak.taskproject.domain.model.Category;
import com.belyak.taskproject.domain.model.CategorySummaryWithTaskCount;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryApiMapper {
    CategoryResponse toResponse(CategorySummaryWithTaskCount summary);

    List<CategoryResponse> toResponseList(List<CategorySummaryWithTaskCount> summaries);

    CreateCategoryResponse toCreateResponse(Category category);
}
