package com.belyak.taskproject.api.v1.dto.response;

import com.belyak.taskproject.api.v1.dto.info.UserInfo;
import com.belyak.taskproject.domain.model.TeamStatus;
import lombok.Builder;

import java.util.Set;
import java.util.UUID;

@Builder
public record TeamInfoResponse(
        UUID id,
        String name,
        TeamStatus status,
        UserInfo owner,
        Set<UserInfo> members
) {
}
