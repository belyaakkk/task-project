package com.belyak.taskproject.domain.service;

import com.belyak.taskproject.api.v1.dto.request.CreateTaskRequest;
import com.belyak.taskproject.api.v1.dto.request.UpdateTaskRequest;
import com.belyak.taskproject.domain.model.Task;
import com.belyak.taskproject.domain.model.TaskStatus;
import com.belyak.taskproject.domain.model.TaskSummary;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;

public interface TaskService {
    List<TaskSummary> findTeamTasks(UUID teamId, TaskStatus taskStatus);

    TaskSummary getTaskDetails(UUID teamId, UUID taskId);

    Task createTask(UUID teamId, CreateTaskRequest request);

    Task updateTask(UUID teamId, UUID taskId, UpdateTaskRequest request) throws AccessDeniedException;

    void updateStatus(UUID teamId, UUID taskId, TaskStatus newStatus) throws AccessDeniedException;

    void assignTask(UUID teamId, UUID taskId, UUID assigneeId) throws AccessDeniedException;

    void deleteTask(UUID teamId, UUID taskId);
}
