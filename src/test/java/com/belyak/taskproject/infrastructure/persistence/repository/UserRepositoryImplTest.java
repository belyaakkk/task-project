package com.belyak.taskproject.infrastructure.persistence.repository;

import com.belyak.taskproject.domain.model.User;
import com.belyak.taskproject.infrastructure.persistence.entity.UserEntity;
import com.belyak.taskproject.infrastructure.persistence.mapper.UserPersistenceMapper;
import com.belyak.taskproject.infrastructure.persistence.repository.impl.UserRepositoryImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryImplTest {

    // ── SUT ─────────────────────────────────────────────────────────────────
    private UserRepositoryImpl repositoryImpl;

    // ── Collaborators ────────────────────────────────────────────────────────
    @Mock private SpringDataUserRepository springDataUserRepository;
    @Mock
    private UserPersistenceMapper userPersistenceMapper;

    // ── Fixtures ─────────────────────────────────────────────────────────────
    private static final UUID USER_ID = UUID.randomUUID();
    private static final String EMAIL   = "alex@example.com";
    private static final String NAME    = "Alex";

    private UserEntity userEntity;
    private User domainUser;

    @BeforeEach
    void setUp() {
        repositoryImpl = new UserRepositoryImpl(
                springDataUserRepository,
                userPersistenceMapper
        );

        userEntity = new UserEntity();
        userEntity.setId(USER_ID);
        userEntity.setName(NAME);
        userEntity.setEmail(EMAIL);

        domainUser = User.builder()
                .id(USER_ID)
                .name(NAME)
                .email(EMAIL)
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // findByEmail()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findByEmail()")
    class FindByEmail {

        @Test
        @DisplayName("User found by email - returns Optional with domain model")
        void whenFound_returnsOptionalWithDomain() {
            when(springDataUserRepository.findByEmail(EMAIL)).thenReturn(Optional.of(userEntity));
            when(userPersistenceMapper.toDomain(userEntity)).thenReturn(domainUser);

            Optional<User> result = repositoryImpl.findByEmail(EMAIL);

            assertThat(result).isPresent().contains(domainUser);
            verify(springDataUserRepository).findByEmail(EMAIL);
            verify(userPersistenceMapper).toDomain(userEntity);
        }

        @Test
        @DisplayName("User not found - returns Optional.empty(), mapper not called")
        void whenMissing_returnsEmptyOptional() {
            when(springDataUserRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            Optional<User> result = repositoryImpl.findByEmail(EMAIL);

            assertThat(result).isEmpty();
            verify(userPersistenceMapper, never()).toDomain(any());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // existsByEmail()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("existsByEmail()")
    class ExistsByEmail {

        @Test
        @DisplayName("Email exists -> true")
        void whenExists_returnsTrue() {
            when(springDataUserRepository.existsByEmail(EMAIL)).thenReturn(true);
            assertThat(repositoryImpl.existsByEmail(EMAIL)).isTrue();
        }

        @Test
        @DisplayName("Email does not exist -> false")
        void whenMissing_returnsFalse() {
            when(springDataUserRepository.existsByEmail(EMAIL)).thenReturn(false);
            assertThat(repositoryImpl.existsByEmail(EMAIL)).isFalse();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // findById()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("User found - returns Optional with domain model")
        void whenFound_returnsOptionalWithDomain() {
            when(springDataUserRepository.findById(USER_ID)).thenReturn(Optional.of(userEntity));
            when(userPersistenceMapper.toDomain(userEntity)).thenReturn(domainUser);

            assertThat(repositoryImpl.findById(USER_ID)).isPresent().contains(domainUser);
        }

        @Test
        @DisplayName("Not found - returns Optional.empty()")
        void whenMissing_returnsEmptyOptional() {
            when(springDataUserRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThat(repositoryImpl.findById(USER_ID)).isEmpty();
            verify(userPersistenceMapper, never()).toDomain(any());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // save()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("save()")
    class Save {

        // ── NEW user (id == null) ─────────────────────────────────────────────
        @Test
        @DisplayName("New user (id == null): mapper creates entity, saves")
        void whenIdIsNull_createsNewEntityViaMappingAndSaves() {
            User newUser = User.builder()
                    .id(null)
                    .name("New")
                    .email("new@example.com")
                    .build();

            UserEntity newEntity = new UserEntity();
            newEntity.setName("New");
            newEntity.setEmail("new@example.com");

            when(userPersistenceMapper.toEntity(newUser))               .thenReturn(newEntity);
            when(springDataUserRepository.save(newEntity))              .thenReturn(userEntity);  // saved version
            when(userPersistenceMapper.toDomain(userEntity))            .thenReturn(domainUser);

            User result = repositoryImpl.save(newUser);

            assertThat(result).isEqualTo(domainUser);
            verify(userPersistenceMapper).toEntity(newUser);
            verify(springDataUserRepository).save(newEntity);
            // findById НЕ вызван — мы не ищем существующую запись
            verify(springDataUserRepository, never()).findById(any());
        }

        // ── EXISTING user (id != null, found) ─────────────────────────────────
        @Test
        @DisplayName("Existing user (id != null, found): updates name only")
        void whenIdIsNotNull_andFound_updatesNameOnly() {
            User updateUser = User.builder()
                    .id(USER_ID)
                    .name("Updated Name")
                    .email(EMAIL)
                    .build();

            when(springDataUserRepository.findById(USER_ID))   .thenReturn(Optional.of(userEntity));
            when(springDataUserRepository.save(userEntity))    .thenReturn(userEntity);
            when(userPersistenceMapper.toDomain(userEntity))   .thenReturn(updateUser);

            User result = repositoryImpl.save(updateUser);

            assertThat(result).isEqualTo(updateUser);
            // entity.name должен быть обновлён
            assertThat(userEntity.getName()).isEqualTo("Updated Name");
            // toEntity НЕ вызван — entity загружена из БД
            verify(userPersistenceMapper, never()).toEntity(any());
        }

        // ── EXISTING user (id != null, NOT found) ─────────────────────────────
        @Test
        @DisplayName("Existing user (id != null, missing): throws EntityNotFoundException")
        void whenIdIsNotNull_andMissing_throwsEntityNotFoundException() {
            User ghost = User.builder()
                    .id(USER_ID)
                    .name("Ghost")
                    .email("ghost@example.com")
                    .build();

            when(springDataUserRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> repositoryImpl.save(ghost))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(springDataUserRepository, never()).save(any());
        }
    }
}