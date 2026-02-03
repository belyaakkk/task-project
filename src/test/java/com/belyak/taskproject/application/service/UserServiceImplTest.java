package com.belyak.taskproject.application.service;

import com.belyak.taskproject.domain.model.User;
import com.belyak.taskproject.domain.port.repository.UserRepository;
import com.belyak.taskproject.web.dto.request.UpdateUserRequest;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceImplTest {

    // ── SUT ─────────────────────────────────────────────────────────────────
    @InjectMocks
    private UserServiceImpl userService;

    // ── Collaborators ────────────────────────────────────────────────────────
    @Mock
    private UserRepository userRepository;

    // ── Captors ──────────────────────────────────────────────────────────────
    @Captor
    private ArgumentCaptor<User> userCaptor;

    // ── Fixtures ─────────────────────────────────────────────────────────────
    private static final UUID USER_ID = UUID.randomUUID();
    private static final String OLD_NAME = "Old Name";
    private static final String NEW_NAME = "New Name";

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = User.builder()
                .id(USER_ID)
                .name(OLD_NAME)
                .email("test@example.com")
                .password("encoded_pass")
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // findUserById()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findUserById()")
    class FindUserById {

        @Test
        @DisplayName("User exists: returns user")
        void shouldReturnUserWhenExists() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));

            User result = userService.findUserById(USER_ID);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(USER_ID);
            assertThat(result.getName()).isEqualTo(OLD_NAME);

            verify(userRepository).findById(USER_ID);
        }

        @Test
        @DisplayName("User missing: throws EntityNotFoundException")
        void shouldThrowExceptionWhenUserNotFound() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.findUserById(USER_ID))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("User not fount"); // Совпадает с текстом в сервисе

            verify(userRepository).findById(USER_ID);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // update()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("User exists: updates name and saves")
        void shouldUpdateUserSuccessfully() {
            UpdateUserRequest request = new UpdateUserRequest(NEW_NAME);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            User result = userService.update(USER_ID, request);

            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();

            assertThat(savedUser.getName()).isEqualTo(NEW_NAME);

            assertThat(result.getName()).isEqualTo(NEW_NAME);
        }

        @Test
        @DisplayName("User missing: throws EntityNotFoundException, never saves")
        void shouldThrowExceptionWhenUserNotFound() {
            UpdateUserRequest request = new UpdateUserRequest(NEW_NAME);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.update(USER_ID, request))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(userRepository).findById(USER_ID);
            verify(userRepository, never()).save(any());
        }
    }
}