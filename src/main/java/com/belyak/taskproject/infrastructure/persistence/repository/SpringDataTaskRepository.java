package com.belyak.taskproject.infrastructure.persistence.repository;

import com.belyak.taskproject.domain.model.TaskStatus;
import com.belyak.taskproject.infrastructure.persistence.entity.TaskEntity;
import com.belyak.taskproject.infrastructure.persistence.projections.TaskInfoProjection;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataTaskRepository extends JpaRepository<TaskEntity, UUID> {

    @EntityGraph(attributePaths = {"category", "tags", "assignee"})
    @Query("SELECT t " +
           "FROM TaskEntity  t " +
           "WHERE t.team.id = :teamId AND t.status = :status")
    List<TaskInfoProjection> findAllByTeamIdAndStatus(
            @Param("teamId") UUID teamId,
            @Param("status") TaskStatus status);

    boolean existsByCategoryId(UUID categoryId);

    boolean existsByTagsId(UUID tagId);
}
