package com.belyak.taskproject.web.controller;

import com.belyak.taskproject.application.mapper.TaskApiMapper;
import com.belyak.taskproject.domain.model.Task;
import com.belyak.taskproject.domain.model.TaskStatus;
import com.belyak.taskproject.domain.port.service.TaskService;
import com.belyak.taskproject.infrastructure.persistence.projections.TaskInfoProjection;
import com.belyak.taskproject.web.dto.request.AssignTaskRequest;
import com.belyak.taskproject.web.dto.request.ChangeTaskStatusRequest;
import com.belyak.taskproject.web.dto.request.CreateTaskRequest;
import com.belyak.taskproject.web.dto.request.UpdateTaskRequest;
import com.belyak.taskproject.web.dto.response.CreateTaskResponse;
import com.belyak.taskproject.web.dto.response.TaskResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Tasks", description = "Task management endpoints")
public class TaskController {

    private final TaskService taskService;
    private final TaskApiMapper taskApiMapper;

    @Operation(
            summary = "Get team tasks",
            description = "Retrieve all tasks for a specific team filtered by status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content),
            @ApiResponse(responseCode = "404", description = "Team not found", content = @Content)
    })
    @GetMapping
    @PreAuthorize("@teamSecurity.isMember(#teamId, principal.id)")
    public ResponseEntity<List<TaskResponse>> getTeamTasks(
            @Parameter(description = "ID of the team", required = true)
            @PathVariable UUID teamId,
            @Parameter(description = "Filter by task status", example = "IN_PROGRESS")
            @RequestParam(required = false, defaultValue = "IN_PROGRESS") TaskStatus status) {
        List<TaskInfoProjection> projections = taskService.findTeamTasks(teamId, status);

        return ResponseEntity.ok(taskApiMapper.toResponseList(projections));
    }

    @Operation(
            summary = "Get task details",
            description = "Retrieve detailed information about a specific task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task details retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content),
            @ApiResponse(responseCode = "404", description = "Task not found", content = @Content)
    })
    @GetMapping(path = "/{taskId}")
    @PreAuthorize("@teamSecurity.isMember(#teamId, principal.id)")
    public ResponseEntity<TaskResponse> getTaskDetails(
            @Parameter(description = "ID of the team", required = true)
            @PathVariable UUID teamId,
            @Parameter(description = "ID of the task", required = true)
            @PathVariable UUID taskId) {
        TaskInfoProjection projection = taskService.getTaskDetails(teamId, taskId);

        return ResponseEntity.ok(taskApiMapper.toResponse(projection));
    }

    @Operation(
            summary = "Create a new task",
            description = "Create a new task within a team. The task will be assigned to the specified category and optionally to a user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or assignee not a team member", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content),
            @ApiResponse(responseCode = "404", description = "Team or category not found", content = @Content)
    })
    @PostMapping
    @PreAuthorize("@teamSecurity.isMember(#teamId, principal.id)")
    public ResponseEntity<CreateTaskResponse> createTask(
            @Parameter(description = "ID of the team", required = true)
            @PathVariable UUID teamId,
            @RequestBody @Valid CreateTaskRequest request) {
        Task createdTask = taskService.createTask(teamId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(taskApiMapper.toCreateResponse(createdTask));
    }

    @Operation(
            summary = "Update a task",
            description = "Update task details such as title, description, priority, due date, category, and tags")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied or task doesn't belong to team", content = @Content),
            @ApiResponse(responseCode = "404", description = "Task not found", content = @Content)
    })
    @PutMapping(path = "/{taskId}")
    @PreAuthorize("@teamSecurity.isMember(#teamId, principal.id)")
    public ResponseEntity<CreateTaskResponse> updateTask(
            @Parameter(description = "ID of the team", required = true)
            @PathVariable UUID teamId,
            @Parameter(description = "ID of the task to update", required = true)
            @PathVariable UUID taskId,
            @RequestBody @Valid UpdateTaskRequest request) throws AccessDeniedException {
        Task updatedTask = taskService.updateTask(teamId, taskId, request);

        return ResponseEntity.ok(taskApiMapper.toCreateResponse(updatedTask));
    }

    @Operation(
            summary = "Change task status",
            description = "Update the status of a task (e.g., from TODO to IN_PROGRESS)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Task status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied or task doesn't belong to team", content = @Content),
            @ApiResponse(responseCode = "404", description = "Task not found", content = @Content)
    })
    @PatchMapping(path = "/{taskId}/status")
    @PreAuthorize("@teamSecurity.isMember(#teamId, principal.id)")
    public ResponseEntity<Void> changeTaskStatus(
            @Parameter(description = "ID of the team", required = true)
            @PathVariable UUID teamId,
            @Parameter(description = "ID of the task", required = true)
            @PathVariable UUID taskId,
            @RequestBody @Valid ChangeTaskStatusRequest request) throws AccessDeniedException {
        taskService.updateStatus(teamId, taskId, request.status());

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Assign task to user",
            description = "Assign a task to a team member or unassign by providing null assigneeId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Task assigned successfully"),
            @ApiResponse(responseCode = "400", description = "User is not a team member", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied or task doesn't belong to team", content = @Content),
            @ApiResponse(responseCode = "404", description = "Task not found", content = @Content)
    })
    @PatchMapping(path = "/{taskId}/assign")
    @PreAuthorize("@teamSecurity.isMember(#teamId, principal.id)")
    public ResponseEntity<Void> assignTask(
            @Parameter(description = "ID of the team", required = true)
            @PathVariable UUID teamId,
            @Parameter(description = "ID of the task", required = true)
            @PathVariable UUID taskId,
            @RequestBody @Valid AssignTaskRequest request) throws AccessDeniedException {
        taskService.assignTask(teamId, taskId, request.assigneeId());

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Delete a task",
            description = "Permanently delete a task from the team")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content),
            @ApiResponse(responseCode = "404", description = "Task not found", content = @Content)
    })
    @DeleteMapping(path = "/{taskId}")
    @PreAuthorize("@teamSecurity.isMember(#teamId, principal.id)")
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "ID of the team", required = true)
            @PathVariable UUID teamId,
            @Parameter(description = "ID of the task to delete", required = true)
            @PathVariable UUID taskId) {
        taskService.deleteTask(teamId, taskId);

        return ResponseEntity.noContent().build();
    }
}
