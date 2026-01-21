package com.belyak.taskproject.domain.model;

import java.util.UUID;

public record TagSummary(
        UUID id,
        String name,
        String color,
        long taskCount
) {
}
