package com.belyak.taskproject.api.v1.controller;

import com.belyak.taskproject.api.v1.dto.request.CreateCategoryRequest;
import com.belyak.taskproject.api.v1.dto.response.CategoryResponse;
import com.belyak.taskproject.api.v1.dto.response.CreateCategoryResponse;
import com.belyak.taskproject.api.v1.mapper.CategoryApiMapper;
import com.belyak.taskproject.domain.model.Category;
import com.belyak.taskproject.domain.model.CategorySummary;
import com.belyak.taskproject.domain.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryApiMapper categoryApiMapper;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategorySummary> allSummaries = categoryService.getAllCategories();
        return ResponseEntity
                .ok(categoryApiMapper.toResponseList(allSummaries));
    }

    @PostMapping
    public ResponseEntity<CreateCategoryResponse> createCategory(
            @Valid @RequestBody CreateCategoryRequest createCategoryRequest) {
        Category categoryToSave = categoryApiMapper.toDomain(createCategoryRequest);
        Category savedCategory = categoryService.createCategory(categoryToSave);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoryApiMapper.toCreateResponse(savedCategory));
    }

    @DeleteMapping(path = "/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity
                .noContent()
                .build();
    }
}
