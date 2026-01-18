package com.belyak.taskproject.api.v1.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@Schema(description = "Payload required to create a new category")
public record CreateCategoryRequest(

        @Schema(description = "The unique name of the category. Must contain only letters, numbers, spaces, and hyphens.",
                example = "Backend Development",
                minLength = 2,
                maxLength = 50)
        @NotBlank(message = "Category name is required.")
        @Size(min = 2, max = 50, message = "Category name must be between {min} and {max} characters.")
        @Pattern(regexp = "^[\\p{L}\\p{N}\\s-]+$", message = "Category name can only contain letters, numbers, spaces and hyphens.")
        String name
) {
}
