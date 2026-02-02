package com.belyak.taskproject.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Standard error response structure")
public record ApiErrorResponse(

        @Schema(description = "HTTP status code", example = "400")
        int status,

        @Schema(description = "Error description", example = "Validation failed")
        String message,

        @Schema(description = "Timestamp of the error occurrence")
        LocalDateTime timestamp,

        @Schema(description = "List of specific field errors, if any")
        List<FieldError> errors
) {
    @Schema(description = "Detailed error for a specific field")
    public record FieldError(

            @Schema(description = "The field that failed validation", example = "name")
            String field,

            @Schema(description = "The error message", example = "Category name is required")
            String message) {
    }

    public static ApiErrorResponse of(int status, String message, List<FieldError> errors) {
        return new ApiErrorResponse(status, message, LocalDateTime.now(), errors);
    }
}
