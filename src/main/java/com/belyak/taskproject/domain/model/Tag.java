package com.belyak.taskproject.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Tag {

    private final UUID id;
    private final String name;
    private final String color;

    public static Tag createNew(String name, String color) {
        validateName(name);
        return new Tag(null, name.trim(), normalizeColor(color));
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tag name cannot be empty");
        }
        if (name.length() < 2 || name.length() > 50) {
            throw new IllegalArgumentException("Tag name must be between 2 and 50 characters.");
        }
    }

    private static String normalizeColor(String color) {
        if (color == null || color.isBlank()) return TagConstants.DEFAULT_COLOR;
        return color.startsWith("#") ? color : "#" + color;
    }
}
