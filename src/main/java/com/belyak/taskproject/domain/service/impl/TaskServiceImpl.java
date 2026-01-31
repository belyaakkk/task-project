package com.belyak.taskproject.domain.service.impl;

import com.belyak.taskproject.api.v1.dto.request.CreateTaskRequest;
import com.belyak.taskproject.api.v1.dto.request.UpdateTaskRequest;
import com.belyak.taskproject.domain.model.Task;
import com.belyak.taskproject.domain.model.TaskPriority;
import com.belyak.taskproject.domain.model.TaskStatus;
import com.belyak.taskproject.domain.model.TaskSummary;
import com.belyak.taskproject.domain.repository.TaskRepository;
import com.belyak.taskproject.domain.repository.TeamRepository;
import com.belyak.taskproject.domain.service.TaskService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TeamRepository teamRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TaskSummary> findTeamTasks(UUID teamId, TaskStatus taskStatus) {
        return taskRepository.findAllByTeamIdAndStatus(teamId, taskStatus);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskSummary getTaskDetails(UUID teamId, UUID taskId) {
        TaskSummary summary = taskRepository.findDetailsById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task with id '%s' not found".formatted(taskId)));

        return summary;
    }

    @Override
    @Transactional
    public Task createTask(UUID teamId, CreateTaskRequest request) {
        if (request.assigneeId() != null && !teamRepository.isMember(teamId, request.assigneeId())) {
            throw new IllegalArgumentException("Assignee is not a member of this team");
        }

        Task newTask = Task.builder()
                .teamId(teamId)
                .title(request.title())
                .description(request.description())
                .status(request.status() != null ? request.status() : TaskStatus.DRAFT)
                .priority(request.priority() != null ? request.priority() : TaskPriority.MEDIUM)
                .dueDate(request.dueDate())
                .categoryId(request.categoryId())
                .tagIds(request.tagIds())
                .assignee(request.assigneeId())
                .build();

        return taskRepository.save(newTask);
    }

    @Override
    @Transactional
    public Task updateTask(UUID teamId, UUID taskId, UpdateTaskRequest request) throws AccessDeniedException {
        Task task = getTaskForEdit(teamId, taskId);

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setPriority(request.priority());
        task.setDueDate(request.dueDate());
        task.setCategoryId(request.categoryId());
        task.setTagIds(request.tagIds());

        return taskRepository.save(task);
    }

    @Override
    @Transactional
    public void updateStatus(UUID teamId, UUID taskId, TaskStatus newStatus) throws AccessDeniedException {
        Task task = getTaskForEdit(teamId, taskId);
        task.setStatus(newStatus);
        taskRepository.save(task);
    }

    @Override
    @Transactional
    public void assignTask(UUID teamId, UUID taskId, UUID assigneeId) throws AccessDeniedException {
        if (assigneeId != null && !teamRepository.isMember(teamId, assigneeId)) {
            throw new IllegalArgumentException("User is not a member of the team");
        }

        Task task = getTaskForEdit(teamId, taskId);
        task.setAssignee(assigneeId);
        taskRepository.save(task);
    }

    @Override
    public void deleteTask(UUID teamId, UUID taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new EntityNotFoundException("Task with id '%s' not found".formatted(taskId));
        }
        taskRepository.deleteById(taskId);
    }

    private Task getTaskForEdit(UUID teamId, UUID taskId) throws AccessDeniedException {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task with id '%s' not found".formatted(taskId)));

        if (!task.getTeamId().equals(teamId)) {
            throw new AccessDeniedException("Task does not belong to the specified team");
        }
        return task;
    }
}
