package com.belyak.taskproject.api.v1.controller;

import com.belyak.taskproject.api.v1.dto.request.AuthRequest;
import com.belyak.taskproject.api.v1.dto.request.RegisterRequest;
import com.belyak.taskproject.api.v1.dto.response.AuthResponse;
import com.belyak.taskproject.domain.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(
                authenticationService.register(
                        registerRequest.name(),
                        registerRequest.email(),
                        registerRequest.password()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest loginRequest) {
        return ResponseEntity.ok(
                authenticationService.authenticate(
                        loginRequest.email(),
                        loginRequest.password())
        );
    }
}
