package com.belyak.taskproject.api.v1.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.UUID;

@Builder
@Schema(description = "Details of a specific tag including task statistics")
public record TagResponse(

        @Schema(description = "Unique identifier of the tag", example = "b23e4567-e89b-12d3-a456-426614174000")
        UUID id,

        @Schema(description = "Label of the tag", example = "Urgent")
        String name,

        String color,

        @Schema(description = "Number of published tasks in this tag", example = "55")
        long taskCount) {
}
