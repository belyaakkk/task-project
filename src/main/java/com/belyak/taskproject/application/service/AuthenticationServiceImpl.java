package com.belyak.taskproject.application.service;

import com.belyak.taskproject.domain.model.User;
import com.belyak.taskproject.domain.port.repository.UserRepository;
import com.belyak.taskproject.domain.port.service.AuthenticationService;
import com.belyak.taskproject.infrastructure.security.jwt.JwtService;
import com.belyak.taskproject.infrastructure.security.jwt.TaskUserDetails;
import com.belyak.taskproject.web.dto.request.AuthRequest;
import com.belyak.taskproject.web.dto.request.RegisterRequest;
import com.belyak.taskproject.web.dto.response.AuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.email()));

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email: " + request.email()));

        String jwtToken = jwtService.generateToken(new TaskUserDetails(user));

        return new AuthResponse(jwtToken, 86400000L);
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalStateException("User with email: " + request.email() + " already exists");
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        User newUser = User.register(request.name(), request.email(), encodedPassword);

        User savedUser = userRepository.save(newUser);

        String jwtToken = jwtService.generateToken(new TaskUserDetails(savedUser));

        return new AuthResponse(jwtToken, 86400000L);
    }
}
