package com.belyak.taskproject.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CodeGeneratorUtilsTest {

    @Test
    @DisplayName("Should generate code of specific length")
    void shouldGenerateCodeOfSpecificLength() {
        String code = CodeGeneratorUtils.generateJoinCode(6);

        assertThat(code).isNotNull();
        assertThat(code).hasSize(6);
    }

    @Test
    @DisplayName("Should contain only allowed characters (A-Z, 0-9)")
    void shouldContainOnlyAllowedCharacters() {
        String code = CodeGeneratorUtils.generateJoinCode(100); // генерируем длинную строку для надежности

        assertThat(code).matches("^[A-Z0-9]+$");
    }
}