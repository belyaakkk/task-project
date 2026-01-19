package com.belyak.taskproject.api.v1.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record TeamSummaryResponse(
        UUID id,
        String name,
        boolean isOwner,
        int memberCount
) {
}
