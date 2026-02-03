package com.belyak.taskproject.application.service;

import com.belyak.taskproject.application.mapper.TeamApiMapper;
import com.belyak.taskproject.common.util.CodeGeneratorUtils;
import com.belyak.taskproject.common.util.UniqueCodeGenerator;
import com.belyak.taskproject.domain.model.Team;
import com.belyak.taskproject.domain.model.TeamStatus;
import com.belyak.taskproject.domain.port.repository.TeamRepository;
import com.belyak.taskproject.domain.port.service.TeamService;
import com.belyak.taskproject.infrastructure.persistence.projections.TeamDetailsProjection;
import com.belyak.taskproject.infrastructure.persistence.projections.TeamSummaryProjection;
import com.belyak.taskproject.web.dto.request.CreateTeamRequest;
import com.belyak.taskproject.web.dto.request.JoinTeamRequest;
import com.belyak.taskproject.web.dto.response.TeamDetailsResponse;
import com.belyak.taskproject.web.dto.response.TeamResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.access.AccessDeniedException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;
    private final UniqueCodeGenerator uniqueCodeGenerator;
    private final TeamApiMapper teamApiMapper;

    @Override
    @Transactional(readOnly = true)
    public List<TeamSummaryProjection> getTeamsByMemberId(UUID memberId) {
        return teamRepository.getAllByMemberId(memberId);
    }

    @Override
    @Transactional
    public Team createTeam(CreateTeamRequest request, UUID ownerId) {
        String joinCode = uniqueCodeGenerator.generate(
                () -> CodeGeneratorUtils.generateJoinCode(6),
                teamRepository::existsByJoinCode);

        String encodedPassword = passwordEncoder.encode(request.password());

        Team newTeam = Team.createNew(request.name(), ownerId, joinCode, encodedPassword);

        return teamRepository.save(newTeam);
    }

    @Override
    @Transactional
    public TeamResponse joinTeam(JoinTeamRequest request, UUID userId) {
        Team team = teamRepository.findByJoinCode(request.joinCode())
                .orElseThrow(() -> new EntityNotFoundException("Team not found with code: " + request.joinCode()));

        if (!team.isPasswordMatch(request.password(), passwordEncoder)) {
            throw new BadCredentialsException("Invalid join code or password");
        }

        team.addMember(userId);
        Team savedTeam = teamRepository.save(team);

        return teamApiMapper.toResponse(savedTeam);
    }

    @Override
    public Team findById(UUID teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team with '%sid' not found: ".formatted(teamId)));
    }

    @Override
    @Transactional(readOnly = true)
    public TeamDetailsResponse getTeamDetails(UUID teamId) {
        TeamDetailsProjection projection = teamRepository.getTeamDetailsById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team with '%s' not found: ".formatted(teamId)));

        return teamApiMapper.toDetailsResponse(projection);
    }

    @Override
    @Transactional
    public void kickMember(UUID teamId, UUID memberId, UUID initiatorId) {
        Team team = findById(teamId);

        if (!team.getOwnerId().equals(initiatorId)) {
            throw new AccessDeniedException("Only owner can kick members");
        }

        team.removeMember(memberId);
        teamRepository.save(team);
    }

    @Override
    @Transactional
    public void cleanupDeletedTeams() {
        Instant retentionLimit = Instant.now().minus(30, ChronoUnit.DAYS);

        int deletedCount = teamRepository.deleteByStatusAndDeletedAtBefore(TeamStatus.DELETED, retentionLimit);
        log.info("Cleaned up {} old teams", deletedCount);
    }
}
