package com.belyak.taskproject.api.v1.controller;

import com.belyak.taskproject.api.v1.dto.response.TaskResponse;
import com.belyak.taskproject.api.v1.mapper.TaskApiMapper;
import com.belyak.taskproject.domain.model.TaskStatus;
import com.belyak.taskproject.domain.model.TaskSummary;
import com.belyak.taskproject.domain.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/teams/{teamId}")
public class TaskController {

    private final TaskService taskService;
    private final TaskApiMapper taskApiMapper;

    @GetMapping(path = "/tasks")
    @PreAuthorize("@teamSecurity.isMember(#teamId, principal.id)")
    public ResponseEntity<List<TaskResponse>> getTeamTasks(
            @PathVariable("teamId") UUID teamId,
            @RequestParam(value = "status", required = false, defaultValue = "IN_PROGRESS") TaskStatus status) {
        List<TaskSummary> taskSummaries = taskService.findTeamTasks(teamId, status);

        return ResponseEntity.ok(
                taskApiMapper.toResponseList(taskSummaries));

    }
}
