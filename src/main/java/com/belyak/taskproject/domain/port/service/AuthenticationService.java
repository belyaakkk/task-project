package com.belyak.taskproject.domain.port.service;

import com.belyak.taskproject.web.dto.request.AuthRequest;
import com.belyak.taskproject.web.dto.request.RegisterRequest;
import com.belyak.taskproject.web.dto.response.AuthResponse;

public interface AuthenticationService {
    AuthResponse authenticate(AuthRequest request);

    AuthResponse register(RegisterRequest request);
}
