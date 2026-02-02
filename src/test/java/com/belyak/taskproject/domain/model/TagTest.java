package com.belyak.taskproject.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TagTest {

    @Test
    @DisplayName("createNew creates a valid tag and trims name")
    void createNew_Success() {
        Tag tag = Tag.createNew("  Java  ", "#FF5733");

        assertThat(tag.getName()).isEqualTo("Java");
        assertThat(tag.getColor()).isEqualTo("#FF5733");
        assertThat(tag.getId()).isNull();
    }

    @Test
    @DisplayName("createNew normalizes color by adding hash")
    void createNew_ColorWithoutHash_AddsHash() {
        Tag tag = Tag.createNew("Bug", "FF0000");
        assertThat(tag.getColor()).isEqualTo("#FF0000");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "  "})
    @DisplayName("createNew uses default color if color is missing")
    void createNew_MissingColor_UsesDefault(String color) {
        Tag tag = Tag.createNew("Feature", color);
        assertThat(tag.getColor()).isEqualTo(TagConstants.DEFAULT_COLOR);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    @DisplayName("createNew throws exception for empty name")
    void createNew_InvalidName_ThrowsException(String name) {
        assertThatThrownBy(() -> Tag.createNew(name, "#000"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tag name cannot be empty");
    }

    @Test
    @DisplayName("createNew throws exception for short name")
    void createNew_ShortName_ThrowsException() {
        assertThatThrownBy(() -> Tag.createNew("A", "#000"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 2 and 50 characters");
    }

    @Test
    @DisplayName("createNew throws exception for long name")
    void createNew_LongName_ThrowsException() {
        String longName = "a".repeat(51);
        assertThatThrownBy(() -> Tag.createNew(longName, "#000"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 2 and 50 characters");
    }
}
