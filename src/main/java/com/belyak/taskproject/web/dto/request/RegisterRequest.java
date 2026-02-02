package com.belyak.taskproject.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@Schema(description = "Payload for user registration")
public record RegisterRequest(

        @Schema(description = "User's full name", example = "John Doe")
        @NotBlank(message = "Name is required.")
        String name,

        @Schema(description = "User's email address", example = "john.doe@example.com")
        @NotBlank(message = "Email is required.")
        @Email(message = "Email should be valid.")
        String email,

        @Schema(description = "Password (min 6 chars)", example = "securePass123")
        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters long")
        String password
) {
}