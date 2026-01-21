package com.belyak.taskproject.api.v1.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.Set;

@Builder
public record CreateTagsRequest(

        @NotEmpty(message = "At least one tag name is required.")
        @Size(max = 10, message = "Maximum {max} tag allowed per request.")
        @Valid
        Set<TagItem> tags) {

    public record TagItem(

            @NotBlank(message = "Tag name is required")
            @Size(min = 2, max = 50, message = "Tag name must be between {min} and {max} characters.")
            @Pattern(regexp = "^[\\p{L}\\p{N}\\s-]+$", message = "Tag name can only contain letters, numbers, spaces and hyphens.")
            String name,

            @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Color must be a valid HEX code (e.g. #FF0000)")
            String color
    ) {
    }
}
