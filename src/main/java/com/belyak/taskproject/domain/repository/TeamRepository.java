package com.belyak.taskproject.domain.repository;

import com.belyak.taskproject.domain.model.Team;
import com.belyak.taskproject.domain.model.TeamSummary;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamRepository {
    List<TeamSummary> getAllByMemberId(UUID memberId);

    Team save(Team team);

    boolean existsByJoinCode(String joinCode);

    Optional<Team> findByJoinCode(String joinCode);

    void addMember(UUID teamId, UUID userId);

    boolean isMember(UUID teamId, UUID userId);

    boolean isOwner(UUID teamId, UUID userId);

    Optional<Team> findById(UUID teamId);

    boolean existsById(UUID teamId);
}
