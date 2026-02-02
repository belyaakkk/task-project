package com.belyak.taskproject.web.controller;

import com.belyak.taskproject.web.dto.request.CreateCategoryRequest;
import com.belyak.taskproject.web.dto.response.CategoryResponse;
import com.belyak.taskproject.web.dto.response.CategoryWithTasksResponse;
import com.belyak.taskproject.application.mapper.CategoryApiMapper;
import com.belyak.taskproject.domain.model.Category;
import com.belyak.taskproject.domain.port.service.CategoryService;
import com.belyak.taskproject.infrastructure.persistence.projections.CategoryInfoWithTaskCountProjection;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Categories", description = "Management of task categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryApiMapper categoryApiMapper;

    @Operation(summary = "Get team categories", description = "Retrieve a list of all categories for a specific team, including task counts.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully."),
            @ApiResponse(responseCode = "403", description = "Access denied.", content = @Content)
    })
    @GetMapping(path = "/teams/{teamId}/categories")
    @PreAuthorize("@teamSecurity.isMember(#teamId, principal.id)")
    public ResponseEntity<List<CategoryWithTasksResponse>> getAllCategories(@PathVariable UUID teamId) {
        List<CategoryInfoWithTaskCountProjection> projections = categoryService.findTeamCategories(teamId);

        return ResponseEntity.ok(categoryApiMapper.toWithTasksResponseList(projections));
    }

    @Operation(summary = "Create a category", description = "Create a new category within a team. Name must be unique within the team.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Category created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content),
            @ApiResponse(responseCode = "409", description = "Category with this name already exists in the team", content = @Content)
    })
    @PostMapping(path = "/teams/{teamId}/categories")
    @PreAuthorize("@teamSecurity.isMember(#teamId, principal.id)")
    public ResponseEntity<CategoryResponse> createCategory(
            @PathVariable UUID teamId,
            @RequestBody @Valid CreateCategoryRequest request) {
        Category savedCategory = categoryService.createCategory(teamId, request.name());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoryApiMapper.toResponse(savedCategory));
    }

    @Operation(summary = "Delete a category", description = "Permanently delete a category. Fails if the category contains tasks or is a system category.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Category cannot be deleted", content = @Content)
    })
    @DeleteMapping(path = "/categories/{categoryId}")
    @PreAuthorize("@categorySecurity.hasAccess(#categoryId, principal.id)")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID categoryId) {
        categoryService.deleteCategory(categoryId);

        return ResponseEntity.noContent().build();
    }
}
