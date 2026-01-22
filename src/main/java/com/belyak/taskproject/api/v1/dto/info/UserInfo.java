package com.belyak.taskproject.api.v1.dto.info;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UserInfo(
        UUID id,
        String name,
        String email
) {
}
