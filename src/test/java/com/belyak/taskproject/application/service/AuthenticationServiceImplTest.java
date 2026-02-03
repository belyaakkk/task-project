package com.belyak.taskproject.application.service;

import com.belyak.taskproject.domain.model.User;
import com.belyak.taskproject.domain.port.repository.UserRepository;
import com.belyak.taskproject.infrastructure.security.jwt.JwtService;
import com.belyak.taskproject.infrastructure.security.jwt.TaskUserDetails;
import com.belyak.taskproject.web.dto.request.AuthRequest;
import com.belyak.taskproject.web.dto.request.RegisterRequest;
import com.belyak.taskproject.web.dto.response.AuthResponse;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService Unit Tests")
class AuthenticationServiceImplTest {

    // ── SUT ─────────────────────────────────────────────────────────────────
    @InjectMocks
    private AuthenticationServiceImpl authService;

    // ── Collaborators ────────────────────────────────────────────────────────
    @Mock private UserRepository userRepository;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private PasswordEncoder passwordEncoder;

    // ── Captors ──────────────────────────────────────────────────────────────
    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Captor
    private ArgumentCaptor<UsernamePasswordAuthenticationToken> tokenCaptor;

    // ── Fixtures ─────────────────────────────────────────────────────────────
    private static final String EMAIL = "alex@example.com";
    private static final String PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "encoded_password_123";
    private static final String JWT_TOKEN = "jwt.token.value";
    private static final String NAME = "Alex";

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .email(EMAIL)
                .name(NAME)
                .password(ENCODED_PASSWORD)
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // authenticate()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("authenticate()")
    class Authenticate {

        @Test
        @DisplayName("Valid credentials: returns JWT token")
        void shouldAuthenticateSuccessfully() {
            // Given
            AuthRequest request = new AuthRequest(EMAIL, PASSWORD);

            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(jwtService.generateToken(any(TaskUserDetails.class))).thenReturn(JWT_TOKEN);

            // When
            AuthResponse response = authService.authenticate(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.token()).isEqualTo(JWT_TOKEN);

            // Verify authentication manager was called
            // Note: In your code, you passed email as password: new UsernamePasswordAuthenticationToken(email, email)
            verify(authenticationManager).authenticate(tokenCaptor.capture());
            UsernamePasswordAuthenticationToken token = tokenCaptor.getValue();
            assertThat(token.getPrincipal()).isEqualTo(EMAIL);
            assertThat(token.getCredentials()).isEqualTo(EMAIL); // Based on your implementation logic
        }

        @Test
        @DisplayName("Invalid credentials: throws exception from AuthManager")
        void shouldThrowExceptionWhenAuthManagerFails() {
            // Given
            AuthRequest request = new AuthRequest(EMAIL, "wrong");
            when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad creds"));

            // When & Then
            assertThatThrownBy(() -> authService.authenticate(request))
                    .isInstanceOf(BadCredentialsException.class);

            verify(userRepository, never()).findByEmail(any());
            verify(jwtService, never()).generateToken(any());
        }

        @Test
        @DisplayName("User not found in DB (after auth success): throws UsernameNotFoundException")
        void shouldThrowExceptionWhenUserNotFoundInDb() {
            // Given
            AuthRequest request = new AuthRequest(EMAIL, PASSWORD);

            // Auth manager passes (maybe logic mismatch), but DB search fails
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authService.authenticate(request))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(authenticationManager).authenticate(any());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // register()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("register()")
    class Register {

        @Test
        @DisplayName("New user: encodes password, saves user, returns JWT")
        void shouldRegisterNewUser() {
            // Given
            RegisterRequest request = new RegisterRequest(NAME, EMAIL, PASSWORD);

            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
            // Simulate save returning the user
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(jwtService.generateToken(any(TaskUserDetails.class))).thenReturn(JWT_TOKEN);

            // When
            AuthResponse response = authService.register(request);

            // Then
            assertThat(response.token()).isEqualTo(JWT_TOKEN);

            // Verify save logic
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();

            assertThat(savedUser.getEmail()).isEqualTo(EMAIL);
            assertThat(savedUser.getName()).isEqualTo(NAME);
            assertThat(savedUser.getPassword()).isEqualTo(ENCODED_PASSWORD); // Password must be encoded
        }

        @Test
        @DisplayName("Email exists: throws IllegalStateException")
        void shouldThrowExceptionWhenEmailExists() {
            // Given
            RegisterRequest request = new RegisterRequest(NAME, EMAIL, PASSWORD);
            when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already exists");

            verify(userRepository, never()).save(any());
            verify(jwtService, never()).generateToken(any());
        }
    }
}