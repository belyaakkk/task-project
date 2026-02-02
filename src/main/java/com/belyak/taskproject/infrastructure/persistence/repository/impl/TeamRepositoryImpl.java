package com.belyak.taskproject.infrastructure.persistence.repository.impl;

import com.belyak.taskproject.domain.model.Team;
import com.belyak.taskproject.domain.model.TeamStatus;
import com.belyak.taskproject.domain.port.repository.TeamRepository;
import com.belyak.taskproject.infrastructure.persistence.entity.TeamEntity;
import com.belyak.taskproject.infrastructure.persistence.entity.UserEntity;
import com.belyak.taskproject.infrastructure.persistence.mapper.TeamPersistenceMapper;
import com.belyak.taskproject.infrastructure.persistence.projections.TeamDetailsProjection;
import com.belyak.taskproject.infrastructure.persistence.projections.TeamSummaryProjection;
import com.belyak.taskproject.infrastructure.persistence.repository.SpringDataTeamRepository;
import com.belyak.taskproject.infrastructure.persistence.repository.SpringDataUserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class TeamRepositoryImpl implements TeamRepository {

    private final SpringDataTeamRepository springDataTeamRepository;
    private final SpringDataUserRepository springDataUserRepository;
    private final TeamPersistenceMapper teamPersistenceMapper;

    @Override
    @Transactional(readOnly = true)
    public List<TeamSummaryProjection> getAllByMemberId(UUID userId) {
        return springDataTeamRepository.findTeamsSummaryByMemberIdAndStatus(userId, TeamStatus.ACTIVE);
    }

    @Override
    @Transactional
    public Team save(Team team) {
        TeamEntity entity;

        if (team.getId() != null) {
            entity = springDataTeamRepository.findById(team.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Team with id '%s' not found".formatted(team.getId())));
        } else {
            entity = new TeamEntity();
        }

        entity.setName(team.getName());
        entity.setJoinCode(team.getJoinCode());
        entity.setPassword(team.getPassword());
        entity.setStatus(team.getStatus());

        UserEntity ownerProxy = springDataUserRepository.getReferenceById(team.getOwnerId());
        entity.setOwner(ownerProxy);

        Set<UserEntity> memberProxies = team.getMemberIds().stream()
                .map(springDataUserRepository::getReferenceById)
                .collect(Collectors.toSet());

        entity.setMembers(memberProxies);
        entity.getMembers().add(ownerProxy);

        TeamEntity savedEntity = springDataTeamRepository.save(entity);
        return teamPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public boolean existsByJoinCode(String joinCode) {
        return springDataTeamRepository.existsByJoinCode(joinCode);
    }

    @Override
    public Optional<Team> findByJoinCode(String joinCode) {
        return springDataTeamRepository.findByJoinCode(joinCode)
                .map(teamPersistenceMapper::toDomain);
    }

    @Override
    public boolean isMember(UUID teamId, UUID userId) {
        return springDataTeamRepository.isMember(teamId, userId);
    }

    @Override
    public boolean isOwner(UUID teamId, UUID userId) {
        return springDataTeamRepository.isOwner(teamId, userId);
    }

    @Override
    public Optional<Team> findById(UUID teamId) {
        return springDataTeamRepository.findById(teamId)
                .map(teamPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsById(UUID teamId) {
        return springDataTeamRepository.existsById(teamId);
    }

    @Override
    @Transactional
    public int deleteByStatusAndDeletedAtBefore(TeamStatus status, Instant date) {
        return springDataTeamRepository.deleteByStatusAndDeletedAtBefore(status, date);
    }

    @Override
    public Optional<TeamDetailsProjection> getTeamDetailsById(UUID teamId) {
        return springDataTeamRepository.findProjectedById(teamId);
    }
}
