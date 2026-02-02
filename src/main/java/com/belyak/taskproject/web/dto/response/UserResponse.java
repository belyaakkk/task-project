package com.belyak.taskproject.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.UUID;

@Builder
@Schema(description = "User information")
public record UserResponse(

        @Schema(description = "Unique identifier of the user", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "User's full name", example = "John Doe")
        String name,

        @Schema(description = "User's email address", example = "john.doe@example.com")
        String email
) {
}