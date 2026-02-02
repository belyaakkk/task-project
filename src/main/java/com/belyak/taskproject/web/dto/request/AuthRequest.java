package com.belyak.taskproject.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@Schema(description = "Payload for user authentication")
public record AuthRequest(

        @Schema(description = "User's email address", example = "john.doe@example.com")
        @NotBlank(message = "Email is required.")
        String email,

        @Schema(description = "User's password", example = "securePass123")
        @NotBlank(message = "Password is required.")
        String password
) {
}