package com.belyak.taskproject.api.v1.controller;

import com.belyak.taskproject.api.v1.dto.request.CreateTagsRequest;
import com.belyak.taskproject.api.v1.dto.response.CreateTagResponse;
import com.belyak.taskproject.api.v1.dto.response.TagResponse;
import com.belyak.taskproject.api.v1.mapper.TagApiMapper;
import com.belyak.taskproject.domain.model.Tag;
import com.belyak.taskproject.domain.model.TagSummary;
import com.belyak.taskproject.domain.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@io.swagger.v3.oas.annotations.tags.Tag(name = "Tags", description = "Management of task tags")
public class TagController {

    private final TagService tagService;
    private final TagApiMapper tagApiMapper;

    @Operation(summary = "Get team tags", description = "Retrieve all tags available for a specific team.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tags retrieved successfully."),
            @ApiResponse(responseCode = "403", description = "Access denied.", content = @Content),
    })
    @GetMapping(path = "/teams/{teamId}/tags")
    @PreAuthorize("@teamSecurity.isMember(#teamId, principal.id)")
    public ResponseEntity<List<TagResponse>> getAllTags(
            @Parameter(description = "ID of the team", required = true)
            @PathVariable UUID teamId) {
        List<TagSummary> allSummaries = tagService.findTeamTags(teamId);

        return ResponseEntity
                .ok(tagApiMapper.toResponseList(allSummaries));
    }

    @Operation(summary = "Create tags", description = "Create multiple tags for a team in a single request. If color is not provided, a default grey will be assigned.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tags created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied.", content = @Content),
            @ApiResponse(responseCode = "409", description = "Tags with these names already exist in the team.", content = @Content)
    })
    @PostMapping(path = "/teams/{teamId}/tags")
    @PreAuthorize("@teamSecurity.isMember(#teamId, principal.id)")
    public ResponseEntity<List<CreateTagResponse>> createTags(
            @Parameter(description = "ID of the team", required = true)
            @PathVariable UUID teamId,
            @RequestBody @Valid CreateTagsRequest request) {
        List<Tag> createdTags = tagService.createTags(teamId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(tagApiMapper.toCreateResponseList(createdTags));
    }

    @Operation(summary = "Delete a tag", description = "Delete a specific tag by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Tag deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
    })
    @DeleteMapping(path = "/tags/{tagId}")
    @PreAuthorize("@tagSecurity.hasAccess(#tagId, principal.id)")
    public ResponseEntity<Void> deleteTag(
            @Parameter(description = "ID of the tag to delete", required = true)
            @PathVariable UUID tagId) {
        tagService.deleteTag(tagId);

        return ResponseEntity
                .noContent()
                .build();
    }
}
