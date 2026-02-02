package com.belyak.taskproject.domain.port.service;

import com.belyak.taskproject.web.dto.request.UpdateUserRequest;
import com.belyak.taskproject.domain.model.User;

import java.util.UUID;

public interface UserService {
    User findUserById(UUID userId);

    User update(UUID userId, UpdateUserRequest request);
}
