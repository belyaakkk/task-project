package com.belyak.taskproject.infrastructure.persistence.repository;

import com.belyak.taskproject.domain.model.TeamStatus;
import com.belyak.taskproject.infrastructure.persistence.entity.TeamEntity;
import com.belyak.taskproject.infrastructure.persistence.projections.TeamDetailsProjection;
import com.belyak.taskproject.infrastructure.persistence.projections.TeamSummaryProjection;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataTeamRepository extends JpaRepository<TeamEntity, UUID> {

    @Query("SELECT " +
           "t.id as id, t.name as name, " +
           "(CASE WHEN t.owner.id = :memberId THEN true ELSE false END) as owner, " +
           "SIZE(t.members) as memberCount " +
           "FROM TeamEntity t " +
           "JOIN t.members m " +
           "WHERE m.id = :memberId AND t.status = :status")
    List<TeamSummaryProjection> findTeamsSummaryByMemberIdAndStatus(UUID memberId, TeamStatus status);

    boolean existsByJoinCode(String joinCode);

    Optional<TeamEntity> findByJoinCode(String joinCode);

    @Modifying
    @Query(value = "INSERT INTO team_members (team_id, user_id) VALUES (:teamId, :userId)", nativeQuery = true)
    void addMemberNative(@Param("teamId") UUID teamId, @Param("userId") UUID userId);

    @Query("SELECT COUNT(t) > 0 " +
           "FROM TeamEntity t " +
           "JOIN t.members m " +
           "WHERE t.id = :teamId AND m.id = :userId")
    boolean isMember(UUID teamId, UUID userId);

    @Query("SELECT COUNT(t) > 0 " +
           "FROM TeamEntity t " +
           "WHERE t.id = :teamId AND t.owner.id = :userId")
    boolean isOwner(@Param("teamId") UUID teamId, @Param("userId") UUID userId);

    void deleteByStatusAndDeletedAtBefore(TeamStatus status, Instant deletedAt);

    @EntityGraph(attributePaths = {"owner", "members"})
    Optional<TeamDetailsProjection> findProjectedById(UUID id);
}
