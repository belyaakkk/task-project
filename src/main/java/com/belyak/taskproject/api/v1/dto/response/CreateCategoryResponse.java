package com.belyak.taskproject.api.v1.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CreateCategoryResponse(
        UUID id,
        String name
) {
}
