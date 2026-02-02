package com.belyak.taskproject.application.service;

import com.belyak.taskproject.domain.model.User;
import com.belyak.taskproject.domain.port.repository.UserRepository;
import com.belyak.taskproject.domain.port.service.UserService;
import com.belyak.taskproject.web.dto.request.UpdateUserRequest;
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
        User user = findUserById(userId);

        User updatedUser = user.updateProfile(request.name());

        return userRepository.save(updatedUser);
    }
}
