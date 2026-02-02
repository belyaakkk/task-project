package com.belyak.taskproject.domain.port.repository;

import com.belyak.taskproject.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findById(UUID userId);

    User save(User user);
}
