package com.belyak.taskproject.domain.port.service;

import com.belyak.taskproject.domain.model.Task;
import com.belyak.taskproject.domain.model.TaskStatus;
import com.belyak.taskproject.infrastructure.persistence.projections.TaskInfoProjection;
import com.belyak.taskproject.web.dto.request.CreateTaskRequest;
import com.belyak.taskproject.web.dto.request.UpdateTaskRequest;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.UUID;

public interface TaskService {

    // --- CREATE / UPDATE ---

    Task createTask(UUID teamId, CreateTaskRequest request);

    Task updateTask(UUID teamId, UUID taskId, UpdateTaskRequest request) throws org.springframework.security.access.AccessDeniedException, java.nio.file.AccessDeniedException;

    void updateStatus(UUID teamId, UUID taskId, TaskStatus newStatus) throws org.springframework.security.access.AccessDeniedException, java.nio.file.AccessDeniedException;

    void assignTask(UUID teamId, UUID taskId, UUID assigneeId) throws AccessDeniedException, java.nio.file.AccessDeniedException;

    void deleteTask(UUID teamId, UUID taskId);

    // --- READ ---

    List<TaskInfoProjection> findTeamTasks(UUID teamId, TaskStatus taskStatus);

    TaskInfoProjection getTaskDetails(UUID teamId, UUID taskId);
}
