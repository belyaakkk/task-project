package com.belyak.taskproject.api.v1.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CreateTagResponse(
        UUID id,
        String name,
        String color) {
}
