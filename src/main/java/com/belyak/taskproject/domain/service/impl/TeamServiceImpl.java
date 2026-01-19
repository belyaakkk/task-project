package com.belyak.taskproject.domain.service.impl;

import com.belyak.taskproject.api.v1.dto.request.CreateTeamRequest;
import com.belyak.taskproject.api.v1.dto.request.JoinTeamRequest;
import com.belyak.taskproject.api.v1.dto.response.JoinTeamResponse;
import com.belyak.taskproject.domain.model.Team;
import com.belyak.taskproject.domain.model.TeamSummary;
import com.belyak.taskproject.domain.repository.TeamRepository;
import com.belyak.taskproject.domain.service.TeamService;
import com.belyak.taskproject.infrastructure.util.CodeGeneratorUtils;
import com.belyak.taskproject.infrastructure.util.UniqueCodeGenerator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;
    private final UniqueCodeGenerator uniqueCodeGenerator;

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
                .ownerId(ownerId)
                .memberIds(Collections.emptySet())
                .build();

        return teamRepository.save(newTeam);
    }

    @Override
    @Transactional
    public JoinTeamResponse joinTeam(JoinTeamRequest request, UUID userId) {
        Team team = teamRepository.findByJoinCode(request.joinCode())
                .orElseThrow(() -> new EntityNotFoundException("Team not found with code: " + request.joinCode()));

        if (team.getMemberIds().contains(userId)) {
            throw new IllegalStateException("You are already a member of this team");
        }

        if (!passwordEncoder.matches(request.password(), team.getPassword())) {
            throw new BadCredentialsException("Invalid join code or password");
        }

        teamRepository.addMember(team.getId(), userId);

        return JoinTeamResponse.builder()
                .teamId(team.getId())
                .name(team.getName())
                .build();
    }
}
