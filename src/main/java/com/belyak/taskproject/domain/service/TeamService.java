package com.belyak.taskproject.domain.service;

import com.belyak.taskproject.api.v1.dto.request.CreateTeamRequest;
import com.belyak.taskproject.api.v1.dto.request.JoinTeamRequest;
import com.belyak.taskproject.api.v1.dto.response.JoinTeamResponse;
import com.belyak.taskproject.domain.model.Team;
import com.belyak.taskproject.domain.model.TeamSummary;

import java.util.List;
import java.util.UUID;

public interface TeamService {
    List<TeamSummary> getTeamsByMemberId(UUID memberId);

    Team createTeam(CreateTeamRequest createTeamRequest, UUID ownerId);

    JoinTeamResponse joinTeam(JoinTeamRequest request, UUID userId);
}
