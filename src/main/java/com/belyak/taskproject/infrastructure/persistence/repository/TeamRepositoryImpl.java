package com.belyak.taskproject.infrastructure.persistence.repository;

import com.belyak.taskproject.domain.model.Team;
import com.belyak.taskproject.domain.model.TeamSummary;
import com.belyak.taskproject.domain.repository.TeamRepository;
import com.belyak.taskproject.infrastructure.persistence.entity.TeamEntity;
import com.belyak.taskproject.infrastructure.persistence.entity.UserEntity;
import com.belyak.taskproject.infrastructure.persistence.mapper.TeamPersistenceMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TeamRepositoryImpl implements TeamRepository {

    private final SpringDataTeamRepository springDataTeamRepository;
    private final SpringDataUserRepository springDataUserRepository;
    private final TeamPersistenceMapper teamPersistenceMapper;

    @Override
    public List<TeamSummary> getAllByMemberId(UUID userId) {
        return teamPersistenceMapper.toSummaryList(
                springDataTeamRepository.findTeamsSummaryByMemberId(userId));
    }

    @Override
    public Team save(Team team) {
        TeamEntity entity = teamPersistenceMapper.toEntity(team);

        UserEntity ownerProxy = springDataUserRepository.getReferenceById(team.getOwnerId());
        entity.setOwner(ownerProxy);

        if (entity.getMembers() == null) {
            entity.setMembers(new HashSet<>());
        }
        entity.getMembers().add(ownerProxy);

        return teamPersistenceMapper.toDomain(springDataTeamRepository.save(entity));
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
    public void addMember(UUID teamId, UUID userId) {
        if (!springDataTeamRepository.existsById(teamId)) {
            throw new EntityNotFoundException("Team not found with id: " + teamId);
        }
        springDataTeamRepository.addMemberNative(teamId, userId);
    }
}
