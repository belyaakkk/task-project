package com.belyak.taskproject.api.v1.controller;

import com.belyak.taskproject.api.v1.annotation.CurrentUserId;
import com.belyak.taskproject.api.v1.dto.request.CreateTeamRequest;
import com.belyak.taskproject.api.v1.dto.request.JoinTeamRequest;
import com.belyak.taskproject.api.v1.dto.response.CreateTeamResponse;
import com.belyak.taskproject.api.v1.dto.response.JoinTeamResponse;
import com.belyak.taskproject.api.v1.dto.response.TeamSummaryResponse;
import com.belyak.taskproject.api.v1.mapper.TeamApiMapper;
import com.belyak.taskproject.domain.model.Team;
import com.belyak.taskproject.domain.model.TeamSummary;
import com.belyak.taskproject.domain.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/teams")
@Tag(name = "Teams", description = "Team management endpoints")
public class TeamController {

    private final TeamService teamService;
    private final TeamApiMapper teamApiMapper;

    @Operation(
            summary = "Get user's teams",
            description = "Return a list of teams where the user is a member or owner")
    @GetMapping
    public ResponseEntity<List<TeamSummaryResponse>> getMyTeams(@CurrentUserId UUID userId) {
        List<TeamSummary> teamSummaries = teamService.getTeamsByMemberId(userId);
        return ResponseEntity.ok(
                teamApiMapper.toSummaryResponseList(teamSummaries));
    }

    @Operation(
            summary = "Create a new team",
            description = "Creates a new team with the authenticated user as the owner. " +
                          "The user is automatically added as a member. " +
                          "Returns the created team details including the generated join code.")
    @PostMapping
    public ResponseEntity<CreateTeamResponse> createTeam(
            @RequestBody @Valid CreateTeamRequest createTeamRequest,
            @CurrentUserId UUID userId) {
        Team createdTeam = teamService.createTeam(createTeamRequest, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(teamApiMapper.toCreateResponse(createdTeam));
    }


    @Operation(
            summary = "Join a team",
            description = "Adds the authenticated user to the team using a join code and password.")
    @PostMapping(path = "/join")
    public ResponseEntity<JoinTeamResponse> joinTeam(
            @RequestBody @Valid JoinTeamRequest request,
            @CurrentUserId UUID userId) {
        return ResponseEntity.ok(teamService.joinTeam(request, userId));
    }
}

