package com.belyak.taskproject.infrastructure.persistence.repository;

import com.belyak.taskproject.domain.model.TaskStatus;
import com.belyak.taskproject.infrastructure.persistence.entity.CategoryEntity;
import com.belyak.taskproject.infrastructure.persistence.projections.CategorySummaryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataCategoryRepository extends JpaRepository<CategoryEntity, UUID> {

    @Query("SELECT c.id as id, c.name as name, COUNT(t) as taskCount " +
           "FROM CategoryEntity c " +
           "LEFT JOIN c.tasks t ON t.status = :status " +
           "GROUP BY c.id, c.name")
    List<CategorySummaryProjection> findCategoriesWithTaskCount(@Param("status") TaskStatus status);

    @Query("SELECT c.id as id, c.name as name, COUNT(t) as taskCount " +
           "FROM CategoryEntity c " +
           "LEFT JOIN c.tasks t ON t.status = :status " +
           "WHERE c.id = :id " +
           "GROUP BY c.id, c.name")
    Optional<CategorySummaryProjection> findCategoryWithTaskCountById(UUID id, @Param("status") TaskStatus status);

    boolean existsByNameIgnoreCase(String name);
}
