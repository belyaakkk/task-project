package com.belyak.taskproject.domain.service.impl;

import com.belyak.taskproject.domain.event.CategoryDeleteEvent;
import com.belyak.taskproject.domain.exception.CategoryAlreadyExistsException;
import com.belyak.taskproject.domain.model.Category;
import com.belyak.taskproject.domain.model.CategorySummary;
import com.belyak.taskproject.domain.model.TaskStatus;
import com.belyak.taskproject.domain.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    @DisplayName("getAllCategories should return summary list with PUBLISHED status filter")
    void getAllCategories_shouldReturnList() {
        // Given
        var summaries = List.of(new CategorySummary(UUID.randomUUID(), "Java", 5));
        when(categoryRepository.findAllSummaries(TaskStatus.PUBLISHED)).thenReturn(summaries);

        // When
        List<CategorySummary> result = categoryService.getAllCategories();

        // Then
        assertThat(result).hasSize(1);
        verify(categoryRepository).findAllSummaries(TaskStatus.PUBLISHED);
    }

    @Test
    @DisplayName("createCategory should save category when name is unique")
    void createCategory_shouldSave_whenNameUnique() {
        // Given
        Category input = Category.builder().name("Java").build();
        Category saved = Category.builder().id(UUID.randomUUID()).name("Java").build();

        when(categoryRepository.existsByNameIgnoreCase("Java")).thenReturn(false);
        when(categoryRepository.save(input)).thenReturn(saved);

        // When
        Category result = categoryService.createCategory(input);

        // Then
        assertThat(result.getId()).isNotNull();
        verify(categoryRepository).save(input);
    }

    @Test
    @DisplayName("createCategory should throw exception when name already exists")
    void createCategory_shouldThrow_whenNameExists() {
        // Given
        Category input = Category.builder().name("Java").build();
        when(categoryRepository.existsByNameIgnoreCase("Java")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> categoryService.createCategory(input))
                .isInstanceOf(CategoryAlreadyExistsException.class)
                .hasMessageContaining("Java");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteCategory should publish event and delete when category exists")
    void deleteCategory_shouldPublishEventAndDelete() {
        // Given
        UUID id = UUID.randomUUID();
        when(categoryRepository.existsById(id)).thenReturn(true);

        // When
        categoryService.deleteCategory(id);

        // Then
        verify(eventPublisher).publishEvent(any(CategoryDeleteEvent.class));
        verify(categoryRepository).deleteById(id);
    }

    @Test
    @DisplayName("deleteCategory should throw exception when category not found")
    void deleteCategory_shouldThrow_whenNotFound() {
        // Given
        UUID id = UUID.randomUUID();
        when(categoryRepository.existsById(id)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> categoryService.deleteCategory(id))
                .isInstanceOf(EntityNotFoundException.class);

        verify(eventPublisher, never()).publishEvent(any());
        verify(categoryRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("findCategoryById should return category when found")
    void findCategoryById_shouldReturnCategory_whenExists() {
        // Given
        UUID id = UUID.randomUUID();
        Category category = Category.builder().id(id).name("Java").build();

        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));

        // When
        Category result = categoryService.findCategoryById(id);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo("Java");

        verify(categoryRepository).findById(id);
    }

    @Test
    @DisplayName("findCategoryById should throw EntityNotFoundException when not found")
    void findCategoryById_shouldThrow_whenNotFound() {
        // Given
        UUID id = UUID.randomUUID();

        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.findCategoryById(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(id.toString());

        verify(categoryRepository).findById(id);
    }
}