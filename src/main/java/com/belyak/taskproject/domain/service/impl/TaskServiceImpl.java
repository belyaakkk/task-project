package com.belyak.taskproject.domain.service.impl;

import com.belyak.taskproject.domain.repository.TaskRepository;
import com.belyak.taskproject.domain.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository postRepository;

    @Override
    public boolean existsByCategoryId(UUID categoryId) {
        return postRepository.existsByCategoryId(categoryId);
    }
}
