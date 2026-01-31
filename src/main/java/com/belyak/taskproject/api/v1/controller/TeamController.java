package com.belyak.taskproject.api.v1.controller;

import com.belyak.taskproject.api.v1.annotation.CurrentUserId;
import com.belyak.taskproject.api.v1.dto.request.CreateTeamRequest;
import com.belyak.taskproject.api.v1.dto.request.JoinTeamRequest;
import com.belyak.taskproject.api.v1.dto.response.*;
import com.belyak.taskproject.api.v1.mapper.TeamApiMapper;
import com.belyak.taskproject.api.v1.mapper.UserApiMapper;
import com.belyak.taskproject.domain.model.Team;
import com.belyak.taskproject.domain.model.TeamSummary;
import com.belyak.taskproject.domain.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/teams")
@Tag(name = "Teams", description = "Team management endpoints")
public class TeamController {

    private final TeamService teamService;
    private final TeamApiMapper teamApiMapper;
    private final UserApiMapper userApiMapper;

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

    @GetMapping(path = "/{teamId}")
    @PreAuthorize("@teamSecurity.isMember(#teamId, principal.id)")
    public ResponseEntity<TeamInfoResponse> getTeamDetails(
            @PathVariable UUID teamId) {
        return ResponseEntity.ok(teamService.getTeamDetails(teamId));
    }

    @GetMapping(path = "/{teamId}/members")
    @PreAuthorize("@teamSecurity.isMember(#teamId, principal.id)")
    public ResponseEntity<Set<UserInfoResponse>> getTeamMembers(
            @PathVariable UUID teamId) {
        Set<UserInfoResponse> members = teamService.getTeamDetails(teamId).members()
                .stream()
                .map(userApiMapper::toInfoResponse)
                .collect(Collectors.toSet());

        return ResponseEntity.ok(members);
    }

    @DeleteMapping(path = "/{teamId}/members/{memberId}")
    @PreAuthorize("@teamSecurity.isOwner(#teamId, principal.id)")
    public ResponseEntity<Void> kickMember(
            @PathVariable UUID teamId,
            @PathVariable UUID memberId,
            @CurrentUserId UUID initiatorId) throws AccessDeniedException {
        teamService.kickMember(teamId, memberId, initiatorId);
        return ResponseEntity.noContent().build();
    }
}

