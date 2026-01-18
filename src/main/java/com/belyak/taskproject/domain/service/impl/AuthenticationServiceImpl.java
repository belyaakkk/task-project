package com.belyak.taskproject.domain.service.impl;

import com.belyak.taskproject.api.v1.dto.response.AuthResponse;
import com.belyak.taskproject.domain.model.Role;
import com.belyak.taskproject.domain.model.User;
import com.belyak.taskproject.domain.repository.UserRepository;
import com.belyak.taskproject.domain.service.AuthenticationService;
import com.belyak.taskproject.infrastructure.security.JwtService;
import com.belyak.taskproject.infrastructure.security.TaskUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse authenticate(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email: " + email));

        String jwtToken = jwtService.generateToken(new TaskUserDetails(user));

        return AuthResponse.builder()
                .token(jwtToken)
                .expiresIn(86400000L)
                .build();
    }

    @Override
    public AuthResponse register(String name, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("User with email: " + email + " already exists");
        }

        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(user);

        String jwtToken = jwtService.generateToken(new TaskUserDetails(savedUser));

        return AuthResponse.builder()
                .token(jwtToken)
                .expiresIn(86400000L)
                .build();
    }
}
