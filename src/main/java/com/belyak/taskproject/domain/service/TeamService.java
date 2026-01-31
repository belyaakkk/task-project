package com.belyak.taskproject.domain.service;

import com.belyak.taskproject.api.v1.dto.request.CreateTeamRequest;
import com.belyak.taskproject.api.v1.dto.request.JoinTeamRequest;
import com.belyak.taskproject.api.v1.dto.response.JoinTeamResponse;
import com.belyak.taskproject.api.v1.dto.response.TeamInfoResponse;
import com.belyak.taskproject.domain.model.Team;
import com.belyak.taskproject.domain.model.TeamSummary;
import com.belyak.taskproject.infrastructure.persistence.projections.TeamDetailsProjection;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;

public interface TeamService {
    // --- WRITE ---

    Team createTeam(CreateTeamRequest createTeamRequest, UUID ownerId);

    JoinTeamResponse joinTeam(JoinTeamRequest request, UUID userId);

    void kickMember(UUID teamId, UUID memberId, UUID initiatorId) throws AccessDeniedException;

    // --- READ: ---

    List<TeamSummary> getTeamsByMemberId(UUID memberId);

    TeamInfoResponse getTeamDetails(UUID teamId);

    // --- INTERNAL / HELPER ---

    Team findById(UUID teamId);
}
