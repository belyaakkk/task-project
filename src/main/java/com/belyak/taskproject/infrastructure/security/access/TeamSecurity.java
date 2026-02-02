package com.belyak.taskproject.infrastructure.security.access;

import com.belyak.taskproject.domain.port.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("teamSecurity")
@RequiredArgsConstructor
public class TeamSecurity {

    private final TeamRepository teamRepository;

    public boolean isMember(UUID teamId, UUID userId) {
        return teamRepository.isMember(teamId, userId);
    }

    public boolean isOwner(UUID teamId, UUID userId) {
        return teamRepository.isOwner(teamId, userId);
    }
}
