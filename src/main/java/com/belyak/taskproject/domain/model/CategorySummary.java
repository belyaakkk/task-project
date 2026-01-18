package com.belyak.taskproject.domain.model;

import java.util.UUID;

public record CategorySummary(
        UUID id,
        String name,
        long taskCount
) {
}
