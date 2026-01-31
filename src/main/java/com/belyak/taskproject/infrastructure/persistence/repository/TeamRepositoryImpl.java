package com.belyak.taskproject.infrastructure.persistence.repository;

import com.belyak.taskproject.domain.model.Team;
import com.belyak.taskproject.domain.model.TeamStatus;
import com.belyak.taskproject.domain.model.TeamSummary;
import com.belyak.taskproject.domain.repository.TeamRepository;
import com.belyak.taskproject.infrastructure.persistence.entity.TeamEntity;
import com.belyak.taskproject.infrastructure.persistence.entity.UserEntity;
import com.belyak.taskproject.infrastructure.persistence.mapper.TeamPersistenceMapper;
import com.belyak.taskproject.infrastructure.persistence.projections.TeamDetailsProjection;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class TeamRepositoryImpl implements TeamRepository {

    private final SpringDataTeamRepository springDataTeamRepository;
    private final SpringDataUserRepository springDataUserRepository;
    private final TeamPersistenceMapper teamPersistenceMapper;

    @Override
    public List<TeamSummary> getAllByMemberId(UUID userId) {
        return teamPersistenceMapper.toSummaryList(
                springDataTeamRepository.findTeamsSummaryByMemberIdAndStatus(userId, TeamStatus.ACTIVE));
    }

    @Override
    public Team save(Team team) {
        TeamEntity entityToSave;

        if (team.getId() != null) {
            // Логика обновления
            entityToSave = springDataTeamRepository.findById(team.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Team with id '%s' not found".formatted(team.getId())));

            entityToSave.setName(team.getName());
            entityToSave.setJoinCode(team.getJoinCode());
            entityToSave.setPassword(team.getPassword());
            entityToSave.setStatus(team.getStatus());
        } else {
            // Логика создания
            entityToSave = new TeamEntity();
            entityToSave.setName(team.getName());

            entityToSave.setJoinCode(team.getJoinCode());
            entityToSave.setPassword(team.getPassword());

            if (team.getStatus() != null) {
                entityToSave.setStatus(team.getStatus());
            }
        }

        UserEntity ownerProxy = springDataUserRepository.getReferenceById(team.getOwner());
        entityToSave.setOwner(ownerProxy);

        Set<UserEntity> memberProxies = new HashSet<>();
        if (team.getMembers() != null && !team.getMembers().isEmpty()) {
            memberProxies = team.getMembers().stream()
                    .map(springDataUserRepository::getReferenceById)
                    .collect(Collectors.toSet());
        }

        entityToSave.setMembers(memberProxies);
        entityToSave.getMembers().add(ownerProxy);

        TeamEntity savedEntity = springDataTeamRepository.save(entityToSave);
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
        return springDataTeamRepository.findById(teamId).map(teamPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsById(UUID teamId) {
        return springDataTeamRepository.existsById(teamId);
    }

    @Override
    public void deleteByStatusAndDeletedAtBefore(TeamStatus status, Instant date) {
        springDataTeamRepository.deleteByStatusAndDeletedAtBefore(status, date);
    }

    @Override
    public Optional<TeamDetailsProjection> getTeamDetailsById(UUID teamId) {
        return springDataTeamRepository.findProjectedById(teamId);
    }
}
