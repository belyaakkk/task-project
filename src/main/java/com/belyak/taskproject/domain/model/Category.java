package com.belyak.taskproject.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Category {

    private final UUID id;
    private final String name;
    private final boolean isSystem;

    public static Category createNew(String name) {
        validateName(name);
        return new Category(null, name.trim(), false);
    }

    public static Category createSystem(String name) {
        validateName(name);
        return new Category(null, name.trim(), true);
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name cannot be empty.");
        }
        if (name.length() < 2 || name.length() > 50) {
            throw new IllegalArgumentException("Category name must be between 2 and 50 characters.");
        }
    }
}
