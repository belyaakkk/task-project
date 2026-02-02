package com.belyak.taskproject.web.controller;

import com.belyak.taskproject.application.mapper.UserApiMapper;
import com.belyak.taskproject.domain.model.User;
import com.belyak.taskproject.domain.port.service.UserService;
import com.belyak.taskproject.web.dto.request.UpdateUserRequest;
import com.belyak.taskproject.web.dto.response.UserResponse;
import com.belyak.taskproject.web.resolver.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/users")
@Tag(name = "Users", description = "User profile management")
public class UserController {

    private final UserService userService;
    private final UserApiMapper userApiMapper;

    @Operation(summary = "Get my profile")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(@Parameter(hidden = true) @CurrentUserId UUID userId) {
        User user = userService.findUserById(userId);
        return ResponseEntity.ok(userApiMapper.toResponse(user));
    }

    @Operation(summary = "Update my profile")
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateUserInfo(
            @Parameter(hidden = true) @CurrentUserId UUID userId,
            @RequestBody @Valid UpdateUserRequest request) {
        User updatedUser = userService.update(userId, request);
        return ResponseEntity.ok(userApiMapper.toResponse(updatedUser));
    }
}
