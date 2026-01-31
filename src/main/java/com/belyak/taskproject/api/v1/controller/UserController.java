package com.belyak.taskproject.api.v1.controller;

import com.belyak.taskproject.api.v1.annotation.CurrentUserId;
import com.belyak.taskproject.api.v1.dto.request.UpdateUserRequest;
import com.belyak.taskproject.api.v1.dto.response.UserInfoResponse;
import com.belyak.taskproject.api.v1.mapper.UserApiMapper;
import com.belyak.taskproject.domain.model.User;
import com.belyak.taskproject.domain.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/users")
public class UserController {

    private final UserService userService;
    private final UserApiMapper userApiMapper;

    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getMe(
            @CurrentUserId UUID userId) {
        User user = userService.findUserById(userId);
        return ResponseEntity
                .ok(userApiMapper.toInfoResponse(user));
    }

    @PutMapping("/me")
    public ResponseEntity<UserInfoResponse> updateUserInfo(
            @CurrentUserId UUID userId,
            @RequestBody @Valid UpdateUserRequest request) {
        User updatedUser = userService.update(userId, request);
        return ResponseEntity
                .ok(userApiMapper.toInfoResponse(updatedUser));
    }
}
