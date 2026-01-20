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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1")
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryApiMapper categoryApiMapper;

    @GetMapping(path = "/teams/{teamId}/categories")
    @PreAuthorize("@teamSecurity.isMember(#teamId, principal.id)")
    public ResponseEntity<List<CategoryResponse>> getAllCategories(
            @PathVariable UUID teamId) {
        List<CategorySummary> allSummaries = categoryService.findTeamCategories(teamId);

        return ResponseEntity
                .ok(categoryApiMapper.toResponseList(allSummaries));
    }

    @PostMapping(path = "/teams/{teamId}/categories")
    @PreAuthorize("@teamSecurity.isMember(#teamId, principal.id)")
    public ResponseEntity<CreateCategoryResponse> createCategory(
            @PathVariable UUID teamId,
            @RequestBody @Valid CreateCategoryRequest request) {
        Category savedCategory = categoryService.createCategory(teamId, request.name());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoryApiMapper.toCreateResponse(savedCategory));
    }

    @DeleteMapping(path = "/categories/{categoryId}")
    @PreAuthorize("@categorySecurity.hasAccess(#categoryId, principal.id)")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable UUID categoryId) {
        categoryService.deleteCategory(categoryId);

        return ResponseEntity
                .noContent()
                .build();
    }
}
