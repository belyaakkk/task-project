package com.belyak.taskproject.domain.repository;

import com.belyak.taskproject.domain.model.Team;
import com.belyak.taskproject.domain.model.TeamStatus;
import com.belyak.taskproject.domain.model.TeamSummary;
import com.belyak.taskproject.infrastructure.persistence.projections.TeamDetailsProjection;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamRepository {
    // --- WRITE ---

    Team save(Team team);

    void deleteByStatusAndDeletedAtBefore(TeamStatus status, Instant deletedAt);

    // --- READ: DOMAIN ---

    Optional<Team> findById(UUID teamId);

    Optional<Team> findByJoinCode(String joinCode);

    // --- READ: PROJECTIONS ---

    List<TeamSummary> getAllByMemberId(UUID memberId);

    Optional<TeamDetailsProjection> getTeamDetailsById(UUID teamId);

    // --- CHECKS & VALIDATION ---

    boolean existsByJoinCode(String joinCode);

    boolean isMember(UUID teamId, UUID userId);

    boolean isOwner(UUID teamId, UUID userId);

    boolean existsById(UUID teamId);

}
