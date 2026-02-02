package com.belyak.taskproject.web.dto.request;

import com.belyak.taskproject.common.validation.HexColor;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.Set;

@Builder
@Schema(description = "Request to create one or multiple tags")
public record CreateTagsRequest(

        @Schema(description = "Set of tags to create")
        @NotEmpty(message = "At least one tag name is required.")
        @Size(max = 10, message = "Maximum {max} tag allowed per request.")
        @Valid
        Set<TagItem> tags) {

    @Schema(description = "Individual tag item")
    public record TagItem(

            @Schema(description = "Name of the tag", example = "Urgent", minLength = 2, maxLength = 50)
            @NotBlank(message = "Tag name is required")
            @Size(min = 2, max = 50, message = "Tag name must be between {min} and {max} characters.")
            @Pattern(regexp = "^[\\p{L}\\p{N}\\s-]+$", message = "Tag name can only contain letters, numbers, spaces and hyphens.")
            String name,

            @Schema(description = "Color in HEX format. If not provided, default grey will be used.", example = "#FF5733")
            @HexColor
            String color
    ) {
    }
}
