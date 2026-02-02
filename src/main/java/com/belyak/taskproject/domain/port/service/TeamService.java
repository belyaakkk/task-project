package com.belyak.taskproject.domain.port.service;

import com.belyak.taskproject.domain.model.Team;
import com.belyak.taskproject.infrastructure.persistence.projections.TeamSummaryProjection;
import com.belyak.taskproject.web.dto.request.CreateTeamRequest;
import com.belyak.taskproject.web.dto.request.JoinTeamRequest;
import com.belyak.taskproject.web.dto.response.TeamDetailsResponse;
import com.belyak.taskproject.web.dto.response.TeamResponse;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.UUID;

public interface TeamService {
    // --- WRITE ---

    Team createTeam(CreateTeamRequest createTeamRequest, UUID ownerId);

    TeamResponse joinTeam(JoinTeamRequest request, UUID userId);

    void kickMember(UUID teamId, UUID memberId, UUID initiatorId) throws AccessDeniedException, java.nio.file.AccessDeniedException;

    void cleanupDeletedTeams();

    // --- READ: ---

    List<TeamSummaryProjection> getTeamsByMemberId(UUID memberId);

    TeamDetailsResponse getTeamDetails(UUID teamId);

    // --- INTERNAL / HELPER ---

    Team findById(UUID teamId);
}
