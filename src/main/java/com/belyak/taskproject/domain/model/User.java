package com.belyak.taskproject.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;
import java.util.regex.Pattern;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {

    private final UUID id;
    private final String name;
    private final String email;
    private final String password; // hashed
    private final Role role;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public static User register(String name, String email, String encodedPassword) {
        String safeName = (name != null) ? name.trim() : null;
        String safeEmail = (email != null) ? email.trim() : null;

        validateName(safeName);
        validateEmail(safeEmail);

        return User.builder()
                .id(null)
                .name(safeName)
                .email(safeEmail.toLowerCase())
                .password(encodedPassword)
                .role(Role.USER)
                .build();
    }

    public static User restore(UUID id, String name, String email, String password, Role role) {
        return User.builder()
                .id(id)
                .name(name)
                .email(email)
                .password(password)
                .role(role)
                .build();
    }

    public User updateProfile(String newName) {
        validateName(newName);

        return this.toBuilder()
                .name(newName.trim())
                .build();
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank() || name.length() < 2) {
            throw new IllegalArgumentException("Name must be at least 2 characters long");
        }
    }

    private static void validateEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }
}
