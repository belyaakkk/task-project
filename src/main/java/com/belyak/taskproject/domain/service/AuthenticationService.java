package com.belyak.taskproject.domain.service;

import com.belyak.taskproject.api.v1.dto.response.AuthResponse;

public interface AuthenticationService {
    AuthResponse authenticate(String email, String password);

    AuthResponse register(String name, String email, String password);
}
