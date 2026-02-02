package com.belyak.taskproject.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record AuthRequest(

        @NotBlank(message = "Email is required.")
        String email,

        @NotBlank(message = "Email is required.")
        String password) {
}
