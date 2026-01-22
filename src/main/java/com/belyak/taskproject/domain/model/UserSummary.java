package com.belyak.taskproject.domain.model;

import java.util.UUID;

public record UserSummary(
        UUID id,
        String name,
        String email
) {
}
