package com.belyak.taskproject.api.v1.dto.response;

import java.util.UUID;

public record UserInfoResponse(
        UUID id,
        String email,
        String name
) {
}
