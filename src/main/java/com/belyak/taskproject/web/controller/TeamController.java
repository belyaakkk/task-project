package com.belyak.taskproject.web.controller;

import com.belyak.taskproject.application.mapper.TeamApiMapper;
import com.belyak.taskproject.domain.model.Team;
import com.belyak.taskproject.domain.port.service.TeamService;
import com.belyak.taskproject.infrastructure.persistence.projections.TeamSummaryProjection;
import com.belyak.taskproject.web.dto.request.CreateTeamRequest;
import com.belyak.taskproject.web.dto.request.JoinTeamRequest;
import com.belyak.taskproject.web.dto.response.TeamDetailsResponse;
import com.belyak.taskproject.web.dto.response.TeamResponse;
import com.belyak.taskproject.web.dto.response.TeamSummaryResponse;
import com.belyak.taskproject.web.dto.response.UserResponse;
import com.belyak.taskproject.web.resolver.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Teams retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<TeamSummaryResponse>> getMyTeams(@CurrentUserId UUID userId) {
        List<TeamSummaryProjection> projections = teamService.getTeamsByMemberId(userId);

        return ResponseEntity.ok(teamApiMapper.toSummaryResponseList(projections));
    }

    @Operation(
            summary = "Create a new team",
            description = "Creates a new team with the authenticated user as the owner. " +
                          "The user is automatically added as a member. " +
                          "Returns the created team details including the generated join code.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Team created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PostMapping
    public ResponseEntity<TeamResponse> createTeam(
            @RequestBody @Valid CreateTeamRequest createTeamRequest,
            @CurrentUserId UUID userId) {
        Team createdTeam = teamService.createTeam(createTeamRequest, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(teamApiMapper.toResponse(createdTeam));
    }


    @Operation(
            summary = "Join a team",
            description = "Adds the authenticated user to the team using a join code and password.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully joined the team"),
            @ApiResponse(responseCode = "400", description = "Invalid join code or password", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Team not found", content = @Content)
    })
    @PostMapping(path = "/join")
    public ResponseEntity<TeamResponse> joinTeam(
            @RequestBody @Valid JoinTeamRequest request,
            @CurrentUserId UUID userId) {
        return ResponseEntity.ok(teamService.joinTeam(request, userId));
    }

    @Operation(
            summary = "Get team details",
            description = "Retrieve detailed information about a specific team including all members.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Team details retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content),
            @ApiResponse(responseCode = "404", description = "Team not found", content = @Content)
    })
    @GetMapping(path = "/{teamId}")
    @PreAuthorize("@teamSecurity.isMember(#teamId, principal.id)")
    public ResponseEntity<TeamDetailsResponse> getTeamDetails(@PathVariable UUID teamId) {
        return ResponseEntity.ok(teamService.getTeamDetails(teamId));
    }

    @Operation(
            summary = "Get team members",
            description = "Retrieve a list of all members in the team.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Members retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content),
            @ApiResponse(responseCode = "404", description = "Team not found", content = @Content)
    })
    @GetMapping(path = "/{teamId}/members")
    @PreAuthorize("@teamSecurity.isMember(#teamId, principal.id)")
    public ResponseEntity<Set<UserResponse>> getTeamMembers(@PathVariable UUID teamId) {
        TeamDetailsResponse teamDetails = teamService.getTeamDetails(teamId);
        Set<UserResponse> members = teamDetails.members();

        return ResponseEntity.ok(members);
    }

    @Operation(
            summary = "Kick a member from the team",
            description = "Remove a member from the team. Only the team owner can perform this action.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Member kicked successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied. Only owner can kick members.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Team or member not found", content = @Content)
    })
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

