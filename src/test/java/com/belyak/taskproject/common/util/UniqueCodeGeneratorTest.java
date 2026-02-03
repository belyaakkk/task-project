package com.belyak.taskproject.common.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UniqueCodeGenerator Unit Tests")
class UniqueCodeGeneratorTest {

    // ── SUT ─────────────────────────────────────────────────────────────────
    private UniqueCodeGenerator uniqueCodeGenerator;

    // ── Collaborators ────────────────────────────────────────────────────────
    @Mock
    private Supplier<String> codeGeneratorMock;

    @Mock
    private Predicate<String> existenceCheckerMock;

    @BeforeEach
    void setUp() {
        uniqueCodeGenerator = new UniqueCodeGenerator();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // generate()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("generate()")
    class Generate {

        @Test
        @DisplayName("First attempt unique: returns code immediately")
        void whenFirstAttemptIsUnique_returnsCode() {
            String expectedCode = "UNIQUE_1";

            when(codeGeneratorMock.get()).thenReturn(expectedCode);
            when(existenceCheckerMock.test(expectedCode)).thenReturn(false);

            String result = uniqueCodeGenerator.generate(codeGeneratorMock, existenceCheckerMock);

            assertThat(result).isEqualTo(expectedCode);

            verify(codeGeneratorMock, times(1)).get();
            verify(existenceCheckerMock, times(1)).test(expectedCode);
        }

        @Test
        @DisplayName("Collisions occur: retries until unique code found")
        void whenCollisionsOccur_retriesAndReturnsUnique() {
            String busyCode1 = "BUSY_1";
            String busyCode2 = "BUSY_2";
            String uniqueCode = "UNIQUE_FINAL";

            when(codeGeneratorMock.get())
                    .thenReturn(busyCode1)
                    .thenReturn(busyCode2)
                    .thenReturn(uniqueCode);

            when(existenceCheckerMock.test(busyCode1)).thenReturn(true);
            when(existenceCheckerMock.test(busyCode2)).thenReturn(true);
            when(existenceCheckerMock.test(uniqueCode)).thenReturn(false);

            String result = uniqueCodeGenerator.generate(codeGeneratorMock, existenceCheckerMock);

            assertThat(result).isEqualTo(uniqueCode);

            verify(codeGeneratorMock, times(3)).get();
            verify(existenceCheckerMock).test(busyCode1);
            verify(existenceCheckerMock).test(busyCode2);
            verify(existenceCheckerMock).test(uniqueCode);
        }

        @Test
        @DisplayName("Max retries exceeded: throws IllegalStateException")
        void whenMaxRetriesExceeded_throwsException() {
            when(codeGeneratorMock.get()).thenReturn("BUSY_CODE");
            when(existenceCheckerMock.test("BUSY_CODE")).thenReturn(true);

            assertThatThrownBy(() -> uniqueCodeGenerator.generate(codeGeneratorMock, existenceCheckerMock))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Failed to generate unique code after 5 attempts");

            verify(codeGeneratorMock, times(6)).get();
            verify(existenceCheckerMock, times(5)).test(anyString());
        }
    }
}