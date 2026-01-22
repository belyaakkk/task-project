package com.belyak.taskproject.domain.model;

import java.util.UUID;

public record CategorySummaryWithTaskCount(
        UUID id,
        String name,
        long taskCount
) {
}
