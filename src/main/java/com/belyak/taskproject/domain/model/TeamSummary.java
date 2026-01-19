package com.belyak.taskproject.domain.model;

import java.util.UUID;

public record TeamSummary(
        UUID id,
        String name,
        boolean isOwner,
        int memberCount
) {
}
