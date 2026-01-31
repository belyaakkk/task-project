package com.belyak.taskproject.domain.service.impl;

import com.belyak.taskproject.api.v1.dto.request.UpdateUserRequest;
import com.belyak.taskproject.domain.model.User;
import com.belyak.taskproject.domain.repository.UserRepository;
import com.belyak.taskproject.domain.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not fount with id: " + userId));
    }

    @Override
    @Transactional
    public User update(UUID userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not fount with id: " + userId));

        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name());
        }

        return userRepository.save(user);
    }
}
