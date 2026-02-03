package com.belyak.taskproject.application.service;

import com.belyak.taskproject.domain.exception.CategoryAlreadyExistsException;
import com.belyak.taskproject.domain.model.Category;
import com.belyak.taskproject.domain.model.TaskStatus;
import com.belyak.taskproject.domain.policy.CategoryDeletionRule;
import com.belyak.taskproject.domain.port.repository.CategoryRepository;
import com.belyak.taskproject.infrastructure.persistence.projections.CategoryInfoWithTaskCountProjection;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryServiceImpl Unit Tests")
class CategoryServiceImplTest {

    // ── SUT ─────────────────────────────────────────────────────────────────
    private CategoryServiceImpl categoryService;

    // ── Collaborators ────────────────────────────────────────────────────────
    @Mock private CategoryRepository categoryRepository;
    @Mock private CategoryDeletionRule deletionRule1;
    @Mock private CategoryDeletionRule deletionRule2;

    // ── Fixtures ─────────────────────────────────────────────────────────────
    private static final UUID TEAM_ID = UUID.randomUUID();
    private static final UUID CATEGORY_ID = UUID.randomUUID();
    private static final String CATEGORY_NAME = "Development";

    private Category category;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryServiceImpl(
                categoryRepository,
                List.of(deletionRule1, deletionRule2)
        );

        category = Category.builder()
                .id(CATEGORY_ID)
                .name(CATEGORY_NAME)
                .isSystem(false)
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // findTeamCategories()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findTeamCategories()")
    class FindTeamCategories {

        @Test
        @DisplayName("Returns list of category projections")
        void shouldFindTeamCategories() {
            CategoryInfoWithTaskCountProjection projection = mock(CategoryInfoWithTaskCountProjection.class);
            when(categoryRepository.findAllByTeamId(eq(TEAM_ID), eq(TaskStatus.IN_PROGRESS)))
                    .thenReturn(List.of(projection));

            List<CategoryInfoWithTaskCountProjection> result = categoryService.findTeamCategories(TEAM_ID);

            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(projection);
            verify(categoryRepository).findAllByTeamId(TEAM_ID, TaskStatus.IN_PROGRESS);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // createCategory()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("createCategory()")
    class CreateCategory {

        @Test
        @DisplayName("Name is unique: creates new Category object and saves it")
        void whenNameIsUnique_createsAndSaves() {
            // Arrange
            String inputName = "  Development  "; // Check trimming logic
            String trimmedName = "Development";

            when(categoryRepository.existsByName(TEAM_ID, inputName)).thenReturn(false);
            when(categoryRepository.createCategory(eq(TEAM_ID), any(Category.class))).thenReturn(category);

            // Act
            Category result = categoryService.createCategory(TEAM_ID, inputName);

            // Assert
            assertThat(result).isEqualTo(category);

            // Verify that the Service created a correct domain object before passing to Repo
            ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
            verify(categoryRepository).createCategory(eq(TEAM_ID), captor.capture());

            Category captured = captor.getValue();
            assertThat(captured.getName()).isEqualTo(trimmedName); // Ensure trim() happened
            assertThat(captured.isSystem()).isFalse(); // Ensure default is false
            assertThat(captured.getId()).isNull(); // ID should be null for new objects
        }

        @Test
        @DisplayName("Name exists: throws CategoryAlreadyExistsException")
        void whenNameExists_throwsException() {
            when(categoryRepository.existsByName(TEAM_ID, CATEGORY_NAME)).thenReturn(true);

            assertThatThrownBy(() -> categoryService.createCategory(TEAM_ID, CATEGORY_NAME))
                    .isInstanceOf(CategoryAlreadyExistsException.class)
                    .hasMessageContaining(CATEGORY_NAME);

            verify(categoryRepository, never()).createCategory(any(), any());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // deleteCategory()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("deleteCategory()")
    class DeleteCategory {

        @Test
        @DisplayName("Category exists and rules pass: deletes category")
        void whenValid_deletesCategory() {
            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
            doNothing().when(deletionRule1).validate(category);
            doNothing().when(deletionRule2).validate(category);

            categoryService.deleteCategory(CATEGORY_ID);

            verify(categoryRepository).deleteById(CATEGORY_ID);
            // Verify rules were called
            verify(deletionRule1).validate(category);
            verify(deletionRule2).validate(category);
        }

        @Test
        @DisplayName("Category not found: throws EntityNotFoundException")
        void whenCategoryNotFound_throwsException() {
            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.deleteCategory(CATEGORY_ID))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(CATEGORY_ID.toString());

            verify(categoryRepository, never()).deleteById(any());
            verify(deletionRule1, never()).validate(any());
        }

        @Test
        @DisplayName("Rule violation: throws exception and does NOT delete")
        void whenRuleFails_throwsExceptionAndAborts() {
            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
            doThrow(new RuntimeException("Cannot delete")).when(deletionRule1).validate(category);

            assertThatThrownBy(() -> categoryService.deleteCategory(CATEGORY_ID))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Cannot delete");

            verify(categoryRepository).findById(CATEGORY_ID);
            verify(deletionRule1).validate(category);
            verify(deletionRule2, never()).validate(any()); // Second rule shouldn't be reached
            verify(categoryRepository, never()).deleteById(any());
        }
    }
}