package com.belyak.taskproject.domain.service.impl;

import com.belyak.taskproject.api.v1.dto.request.CreateTeamRequest;
import com.belyak.taskproject.api.v1.dto.request.JoinTeamRequest;
import com.belyak.taskproject.api.v1.dto.response.JoinTeamResponse;
import com.belyak.taskproject.api.v1.dto.response.TeamInfoResponse;
import com.belyak.taskproject.api.v1.mapper.TeamApiMapper;
import com.belyak.taskproject.domain.model.Team;
import com.belyak.taskproject.domain.model.TeamStatus;
import com.belyak.taskproject.domain.model.TeamSummary;
import com.belyak.taskproject.domain.repository.TeamRepository;
import com.belyak.taskproject.domain.service.TeamService;
import com.belyak.taskproject.infrastructure.persistence.projections.TeamDetailsProjection;
import com.belyak.taskproject.infrastructure.util.CodeGeneratorUtils;
import com.belyak.taskproject.infrastructure.util.UniqueCodeGenerator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;
    private final UniqueCodeGenerator uniqueCodeGenerator;
    private final TeamApiMapper teamApiMapper;

    @Override
    @Transactional(readOnly = true)
    public List<TeamSummary> getTeamsByMemberId(UUID memberId) {
        return teamRepository.getAllByMemberId(memberId);
    }

    @Override
    @Transactional
    public Team createTeam(CreateTeamRequest request, UUID ownerId) {
        String joinCode = uniqueCodeGenerator.generate(
                () -> CodeGeneratorUtils.generateJoinCode(6),
                teamRepository::existsByJoinCode);

        String encodedPassword = passwordEncoder.encode(request.password());

        Team newTeam = Team.builder()
                .name(request.name())
                .joinCode(joinCode)
                .password(encodedPassword)
                .owner(ownerId)
                .status(TeamStatus.ACTIVE)
                .members(Collections.emptySet())
                .build();

        return teamRepository.save(newTeam);
    }

    @Override
    @Transactional
    public JoinTeamResponse joinTeam(JoinTeamRequest request, UUID userId) {
        Team team = teamRepository.findByJoinCode(request.joinCode())
                .orElseThrow(() -> new EntityNotFoundException("Team not found with code: " + request.joinCode()));

        if (team.getMembers().contains(userId)) {
            throw new IllegalStateException("You are already a member of this team");
        }

        if (!passwordEncoder.matches(request.password(), team.getPassword())) {
            throw new BadCredentialsException("Invalid join code or password");
        }

        team.getMembers().add(userId);
        teamRepository.save(team);

        return JoinTeamResponse.builder()
                .teamId(team.getId())
                .name(team.getName())
                .build();
    }

    @Override
    public Team findById(UUID teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team with '%sid' not found: ".formatted(teamId)));
    }

    @Override
    @Transactional(readOnly = true)
    public TeamInfoResponse getTeamDetails(UUID teamId) {
        TeamDetailsProjection projection = teamRepository.getTeamDetailsById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team with '%s' not found: ".formatted(teamId)));

        return teamApiMapper.toInfoResponse(projection);
    }

    @Override
    @Transactional
    public void kickMember(UUID teamId, UUID memberId, UUID initiatorId) throws AccessDeniedException {
        Team team = findById(teamId);

        if (!team.getOwner().equals(initiatorId)) {
            throw new AccessDeniedException("Only the owner can kick members");
        }

        if (memberId.equals(team.getOwner())) {
            throw new IllegalStateException("Owner cannot leave the team via kick action");
        }

        if (!team.getMembers().contains(memberId)) {
            throw new EntityNotFoundException("User is not a member of this team");
        }

        team.getMembers().remove(memberId);

        teamRepository.save(team);
    }
}
