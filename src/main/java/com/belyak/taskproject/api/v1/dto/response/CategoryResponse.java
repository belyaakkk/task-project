package com.belyak.taskproject.api.v1.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.UUID;

@Builder
@Schema(description = "Details of a specific category including task statistics")
public record CategoryResponse(

        @Schema(description = "Unique identifier of the category", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "Name of the category", example = "Java Learning")
        String name,

        @Schema(description = "Number of published tasks in this category", example = "42")
        long taskCount) {
}
