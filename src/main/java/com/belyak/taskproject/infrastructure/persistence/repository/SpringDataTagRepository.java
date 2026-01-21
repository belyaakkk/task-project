package com.belyak.taskproject.infrastructure.persistence.repository;

import com.belyak.taskproject.domain.model.TaskStatus;
import com.belyak.taskproject.infrastructure.persistence.entity.TagEntity;
import com.belyak.taskproject.infrastructure.persistence.projections.TagSummaryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface SpringDataTagRepository extends JpaRepository<TagEntity, UUID> {

    @Query("SELECT t.id as id, t.name as name, t.color as color, COUNT(task) as taskCount " +
           "FROM TagEntity t " +
           "LEFT JOIN t.tasks task on task.status = :status " +
           "WHERE t.team.id = :teamId " +
           "GROUP BY t.id, t.name, t.color")
    List<TagSummaryProjection> findTagsByTeamIdAndStatus(
            @Param("teamId") UUID teamId,
            @Param("status") TaskStatus status);

    @Query("SELECT COUNT(t) > 0 " +
           "FROM TagEntity t " +
           "JOIN t.team team " +
           "JOIN team.members member " +
           "WHERE t.id = :tagId and member.id = :userId")
    boolean canAccess(@Param("tagId") UUID tagId, @Param("userId") UUID userId);

    @Query("SELECT t.name FROM TagEntity t WHERE t.team.id = :teamId")
    Set<String> findTagsNameByTeamId(@Param("teamId") UUID teamId);
}
