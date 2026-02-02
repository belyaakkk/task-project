package com.belyak.taskproject.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.UUID;

@Builder
@Schema(description = "Tag information")
public record TagResponse(

        @Schema(description = "Unique identifier of the tag", example = "b23e4567-e89b-12d3-a456-426614174000")
        UUID id,

        @Schema(description = "Label of the tag", example = "Urgent")
        String name,

        @Schema(description = "Color of the tag in HEX format", example = "#FF5733")
        String color) {
}
