package com.belyak.taskproject.api.v1.dto.response;

import lombok.Builder;

@Builder
public record AuthResponse(
        String token,
        long expiresIn
) {
}
