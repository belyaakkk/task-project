package com.belyak.taskproject.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CategoryTest {

    @Test
    @DisplayName("createNew creates non-system category")
    void createNew_Success() {
        Category category = Category.createNew("  Work  ");

        assertThat(category.getName()).isEqualTo("Work");
        assertThat(category.isSystem()).isFalse();
        assertThat(category.getId()).isNull();
    }

    @Test
    @DisplayName("createSystem creates system category")
    void createSystem_Success() {
        Category category = Category.createSystem("Inbox");

        assertThat(category.getName()).isEqualTo("Inbox");
        assertThat(category.isSystem()).isTrue();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    @DisplayName("createNew throws exception for empty name")
    void createNew_InvalidName_ThrowsException(String name) {
        assertThatThrownBy(() -> Category.createNew(name))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Category name cannot be empty.");
    }


    @Test
    @DisplayName("createNew throws exception for short name")
    void validateName_ThrowsException() {
        assertThatThrownBy(() -> Category.createNew("A"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Category name must be between 2 and 50 characters.");
    }

    @Test
    @DisplayName("createNew throws exception for long name")
    void createNew_LongName_ThrowsException() {
        String longName = "a".repeat(51);
        assertThatThrownBy(() -> Category.createNew(longName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Category name must be between 2 and 50 characters.");
    }
}
