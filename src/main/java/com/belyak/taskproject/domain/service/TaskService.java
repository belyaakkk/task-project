package com.belyak.taskproject.domain.service;

import com.belyak.taskproject.domain.model.TaskStatus;
import com.belyak.taskproject.domain.model.TaskSummary;

import java.util.List;
import java.util.UUID;

public interface TaskService {
    List<TaskSummary> findTeamTasks(UUID teamId, TaskStatus taskStatus);
}
