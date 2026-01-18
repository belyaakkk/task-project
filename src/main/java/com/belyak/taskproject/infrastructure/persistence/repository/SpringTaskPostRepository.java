package com.belyak.taskproject.infrastructure.persistence.repository;

import com.belyak.taskproject.infrastructure.persistence.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringTaskPostRepository extends JpaRepository<TaskEntity, UUID> {

    boolean existsByCategoryId(UUID categoryId);
}
