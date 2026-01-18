package com.belyak.taskproject.domain.repository;

import java.util.UUID;

public interface TaskRepository {
    boolean existsByCategoryId(UUID categoryId);
}
