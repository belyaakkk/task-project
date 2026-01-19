package com.belyak.taskproject.infrastructure.persistence.repository;

import com.belyak.taskproject.infrastructure.persistence.entity.TeamEntity;
import com.belyak.taskproject.infrastructure.persistence.projections.TeamSummaryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
           "WHERE m.id = :memberId")
    List<TeamSummaryProjection> findTeamsSummaryByMemberId(UUID memberId);

    boolean existsByJoinCode(String joinCode);

    Optional<TeamEntity> findByJoinCode(String joinCode);

    @Modifying
    @Query(value = "INSERT INTO team_members (team_id, user_id) VALUES (:teamId, :userId)", nativeQuery = true)
    void addMemberNative(@Param("teamId") UUID teamId, @Param("userId") UUID userId);
}
