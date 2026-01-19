package com.belyak.taskproject.api.v1.dto.request;

import jakarta.validation.constraints.NotBlank;

public record JoinTeamRequest(
        @NotBlank(message = "Join code is required.")
        String joinCode,

        @NotBlank(message = "Password is required.")
        String password
) {
}
