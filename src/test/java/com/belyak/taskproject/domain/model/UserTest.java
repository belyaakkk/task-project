package com.belyak.taskproject.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UserTest {

    @Test
    @DisplayName("register creates user with trimmed name, lowercase email, and default role")
    void register_Success() {
        // Given
        String rawName = "   Joe Doe   ";
        String rawEmail = "   JOE@Example.com   ";
        String password = "hashedPass";

        // When
        User user = User.register(rawName, rawEmail, password);

        // Then
        assertThat(user.getId()).isNull();
        assertThat(user.getName()).isEqualTo("Joe Doe");
        assertThat(user.getEmail()).isEqualTo("joe@example.com");
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getRole()).isEqualTo(Role.USER);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   ", "A"})
    @DisplayName("register throws exception when name is invalid (null, empty, or too short)")
    void register_InvalidName_ThrowsException(String invalidName) {
        assertThatThrownBy(() -> User.register(invalidName, "valid@email.com", "pass"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name must be at least 2 characters long");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"invalid-email", "joe@", "@example.com", "joe doe@example.com"})
    @DisplayName("register throws exception when email format is invalid")
    void register_InvalidEmail_ThrowsException(String invalidEmail) {
        assertThatThrownBy(() -> User.register("Joe", invalidEmail, "pass"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email format");
    }

    @Test
    @DisplayName("restore recreates user object with exact provided fields")
    void restore_Success() {
        // Given
        UUID id = UUID.randomUUID();
        String name = "Admin User";
        String email = "ADMIN@TEST.COM";
        String password = "secretHash";
        Role role = Role.ADMIN;

        // When
        User user = User.restore(id, name, email, password, role);

        // Then
        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getName()).isEqualTo(name);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getRole()).isEqualTo(role);
    }

    @Test
    @DisplayName("updateProfile updates name, returns new instance, and preserves other fields")
    void updateProfile_Success() {
        // Given
        User originalUser = User.register("Old Name", "test@test.com", "pass");
        String newName = "   New Name   ";

        // When
        User updatedUser = originalUser.updateProfile(newName);

        // Then
        assertThat(updatedUser.getName()).isEqualTo("New Name");
        assertThat(updatedUser.getEmail()).isEqualTo("test@test.com");
        assertThat(updatedUser.getId()).isEqualTo(originalUser.getId());

        assertThat(originalUser.getName()).isEqualTo("Old Name");
        assertThat(updatedUser).isNotSameAs(originalUser);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "  ", "B"})
    @DisplayName("updateProfile throws exception when new name is invalid")
    void updateProfile_InvalidName_ThrowsException(String invalidName) {
        User user = User.register("Valid Name", "test@test.com", "pass");

        assertThatThrownBy(() -> user.updateProfile(invalidName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name must be at least 2 characters long");
    }
}
