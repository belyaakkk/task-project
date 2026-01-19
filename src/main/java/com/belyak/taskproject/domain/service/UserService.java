package com.belyak.taskproject.domain.service;

import com.belyak.taskproject.domain.model.User;

import java.util.UUID;

public interface UserService {
    User findUserById(UUID userId);
}
