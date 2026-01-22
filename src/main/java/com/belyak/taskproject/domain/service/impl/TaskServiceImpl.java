package com.belyak.taskproject.domain.service.impl;

import com.belyak.taskproject.domain.model.TaskStatus;
import com.belyak.taskproject.domain.model.TaskSummary;
import com.belyak.taskproject.domain.repository.TaskRepository;
import com.belyak.taskproject.domain.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TaskSummary> findTeamTasks(UUID teamId, TaskStatus taskStatus) {
        return taskRepository.findAllByTeamIdAndStatus(teamId, taskStatus);
    }
}
