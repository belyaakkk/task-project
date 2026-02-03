package com.belyak.taskproject.application.service;

import com.belyak.taskproject.domain.model.Task;
import com.belyak.taskproject.domain.model.TaskStatus;
import com.belyak.taskproject.domain.port.repository.TaskRepository;
import com.belyak.taskproject.domain.port.repository.TeamRepository;
import com.belyak.taskproject.domain.port.service.TaskService;
import com.belyak.taskproject.infrastructure.persistence.projections.TaskInfoProjection;
import com.belyak.taskproject.web.dto.request.CreateTaskRequest;
import com.belyak.taskproject.web.dto.request.UpdateTaskRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TeamRepository teamRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TaskInfoProjection> findTeamTasks(UUID teamId, TaskStatus taskStatus) {
        return taskRepository.findAllByTeamIdAndStatus(teamId, taskStatus);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskInfoProjection getTaskDetails(UUID teamId, UUID taskId) {
        return taskRepository.findDetailsById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task with id '%s' not found".formatted(taskId)));
    }

    @Override
    @Transactional
    public Task createTask(UUID teamId, CreateTaskRequest request) {
        validateAssignee(teamId, request.assigneeId());

        Task newTask = Task.createNew(
                teamId,
                request.title(),
                request.description(),
                request.status(),
                request.priority(),
                request.dueDate(),
                request.categoryId(),
                request.assigneeId(),
                request.tagIds()
        );

        return taskRepository.save(newTask);
    }

    @Override
    @Transactional
    public Task updateTask(UUID teamId, UUID taskId, UpdateTaskRequest request) {
        Task task = getTaskOrThrow(teamId, taskId);

        Task updatedTask = task.updateDetails(
                request.title(),
                request.description(),
                request.priority(),
                request.dueDate(),
                request.assigneeId(),
                request.categoryId(),
                request.tagIds()
        );

        return taskRepository.save(updatedTask);
    }

    @Override
    @Transactional
    public void updateStatus(UUID teamId, UUID taskId, TaskStatus newStatus) {
        Task task = getTaskOrThrow(teamId, taskId);
        Task updatedTask = task.changeStatus(newStatus);
        taskRepository.save(updatedTask);
    }

    @Override
    @Transactional
    public void assignTask(UUID teamId, UUID taskId, UUID assigneeId) {
        Task task = getTaskOrThrow(teamId, taskId);
        if (assigneeId != null) {
            validateAssignee(teamId, assigneeId);
            task = task.assign(assigneeId);
        } else {
            task = task.unassign();
        }

        taskRepository.save(task);
    }

    @Override
    public void deleteTask(UUID teamId, UUID taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new EntityNotFoundException("Task with id '%s' not found".formatted(taskId));
        }
        taskRepository.deleteById(taskId);
    }

    private Task getTaskOrThrow(UUID teamId, UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task with id '%s' not found".formatted(taskId)));

        if (!task.getTeamId().equals(teamId)) {
            throw new AccessDeniedException("Task does not belong to the specified team");
        }
        return task;
    }

    private void validateAssignee(UUID teamId, UUID assigneeId) {
        if (assigneeId != null && !teamRepository.isMember(teamId, assigneeId)) {
            throw new IllegalArgumentException("Assignee must be a member of the team");
        }
    }
}
