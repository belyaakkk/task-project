package com.belyak.taskproject.domain.service;

import java.util.UUID;

public interface TaskService {
    boolean existsByCategoryId(UUID categoryId);
}
