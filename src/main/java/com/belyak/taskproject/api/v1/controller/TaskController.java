package com.belyak.taskproject.api.v1.controller;

import com.belyak.taskproject.api.v1.dto.request.AssignTaskRequest;
import com.belyak.taskproject.api.v1.dto.request.ChangeTaskStatusRequest;
import com.belyak.taskproject.api.v1.dto.request.CreateTaskRequest;
import com.belyak.taskproject.api.v1.dto.request.UpdateTaskRequest;
import com.belyak.taskproject.api.v1.dto.response.CreateTaskResponse;
import com.belyak.taskproject.api.v1.dto.response.TaskResponse;
import com.belyak.taskproject.api.v1.mapper.TaskApiMapper;
import com.belyak.taskproject.domain.model.Task;
import com.belyak.taskproject.domain.model.TaskStatus;
import com.belyak.taskproject.domain.model.TaskSummary;
import com.belyak.taskproject.domain.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/teams/{teamId}/tasks")
public class TaskController {

    private final TaskService taskService;
    private final TaskApiMapper taskApiMapper;

    @GetMapping
    @PreAuthorize("@teamSecurity.isMember(#teamId, principal.id)")
    public ResponseEntity<List<TaskResponse>> getTeamTasks(
            @PathVariable("teamId") UUID teamId,
            @RequestParam(value = "status", required = false, defaultValue = "IN_PROGRESS") TaskStatus status) {
        List<TaskSummary> taskSummaries = taskService.findTeamTasks(teamId, status);

        return ResponseEntity.ok(
                taskApiMapper.toResponseList(taskSummaries));
    }


    //    @PostMapping("/tasks") создать задачу в команде
    @PostMapping
    @PreAuthorize("@teamSecurity.isMember(#teamId, principal.id)")
    public ResponseEntity<CreateTaskResponse> createTask(
            @PathVariable UUID teamId,
            @RequestBody @Valid CreateTaskRequest request) {
        Task task = taskService.createTask(teamId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(taskApiMapper.toCreateResponse(task));
    }


    //    @GetMapping получить детали задачи
    @GetMapping("/{taskId}")
    @PreAuthorize("@teamSecurity.isMember(#teamId, principal.id)")
    public ResponseEntity<TaskResponse> getTask(
            @PathVariable UUID teamId,
            @PathVariable UUID taskId) {
        TaskSummary summary = taskService.getTaskDetails(teamId, taskId);
        return ResponseEntity.ok(taskApiMapper.toResponse(summary));
    }

    //    @PutMapping полностью обновить задачу
    @PutMapping("/{taskId}")
    @PreAuthorize("@teamSecurity.isMember(#teamId, principal.id)")
    public ResponseEntity<CreateTaskResponse> updateTask(
            @PathVariable UUID teamId,
            @PathVariable UUID taskId,
            @RequestBody @Valid UpdateTaskRequest request
    ) throws AccessDeniedException {
        Task updatedTask = taskService.updateTask(teamId, taskId, request);
        return ResponseEntity.ok(taskApiMapper.toCreateResponse(updatedTask));
    }

    //    @PatchMapping("/status") быстро поменять статус(Drag & Drop на канбане)
    @PatchMapping("/{taskId}/status")
    @PreAuthorize("@teamSecurity.isMember(#teamId, principal.id)")
    public ResponseEntity<Void> updateStatus(
            @PathVariable UUID teamId,
            @PathVariable UUID taskId,
            @RequestBody ChangeTaskStatusRequest request) throws AccessDeniedException {
        taskService.updateStatus(teamId, taskId, request.status());
        return ResponseEntity.noContent().build();
    }

    //    @PatchMapping("/assignee") назначить исполнителя
    @PatchMapping("/{taskId}/assignee")
    @PreAuthorize("@teamSecurity.isMember(#teamId, principal.id)")
    public ResponseEntity<Void> assignTask(
            @PathVariable UUID teamId,
            @PathVariable UUID taskId,
            @RequestBody AssignTaskRequest request) throws AccessDeniedException {
        taskService.assignTask(teamId, taskId, request.assigneeId());
        return ResponseEntity.noContent().build();
    }

    //    @DeleteMapping удалить задачу (архивировать)
    @DeleteMapping("/{taskId}")
    @PreAuthorize("@teamSecurity.isOwner(#teamId, principal.id)") // Удалять может только владелец? Или автор?
    public ResponseEntity<Void> deleteTask(
            @PathVariable UUID teamId,
            @PathVariable UUID taskId) {
        taskService.deleteTask(teamId, taskId);
        return ResponseEntity.noContent().build();
    }

}
