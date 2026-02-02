package com.belyak.taskproject.infrastructure.persistence.repository.impl;

import com.belyak.taskproject.domain.model.User;
import com.belyak.taskproject.domain.port.repository.UserRepository;
import com.belyak.taskproject.infrastructure.persistence.entity.UserEntity;
import com.belyak.taskproject.infrastructure.persistence.mapper.UserPersistenceMapper;
import com.belyak.taskproject.infrastructure.persistence.repository.SpringDataUserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;
    private final UserPersistenceMapper userPersistenceMapper;

    @Override
    public Optional<User> findByEmail(String email) {
        return springDataUserRepository.findByEmail(email)
                .map(userPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataUserRepository.existsByEmail(email);
    }

    @Override
    public Optional<User> findById(UUID userId) {
        return springDataUserRepository.findById(userId)
                .map(userPersistenceMapper::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity entityToSave;
        if (user.getId() != null) {
            entityToSave = springDataUserRepository.findById(user.getId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
            entityToSave.setName(user.getName());
        } else {
            entityToSave = userPersistenceMapper.toEntity(user);
        }

        UserEntity savedEntity = springDataUserRepository.save(entityToSave);
        return userPersistenceMapper.toDomain(savedEntity);
    }
}
