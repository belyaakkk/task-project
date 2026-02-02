package com.belyak.taskproject.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TaskTest {

    private final UUID teamId = UUID.randomUUID();
    private final UUID assigneeId = UUID.randomUUID();
    private final UUID categoryId = UUID.randomUUID();

    @Test
    @DisplayName("createNew initializes task with defaults (Draft status, Medium priority, empty desc/tags)")
    void createNew_Defaults_Success() {
        // Given
        String title = "   Fix Bug   ";

        Task task = Task.createNew(
                teamId, title, null, null, null, null, categoryId, null, null
        );

        // Then
        assertThat(task.getId()).isNull();
        assertThat(task.getTeamId()).isEqualTo(teamId);
        assertThat(task.getTitle()).isEqualTo("Fix Bug");
        assertThat(task.getDescription()).isEmpty();
        assertThat(task.getStatus()).isEqualTo(TaskStatus.DRAFT);
        assertThat(task.getPriority()).isEqualTo(TaskPriority.MEDIUM);
        assertThat(task.getCreatedAt()).isNotNull();
        assertThat(task.getTagIds()).isNotNull().isEmpty();
        assertThat(task.getAssigneeId()).isNull();
    }

    @Test
    @DisplayName("createNew initializes task with all provided values")
    void createNew_Full_Success() {
        // Given
        Instant dueDate = Instant.now().plusSeconds(3600);
        UUID tagId = UUID.randomUUID();
        Set<UUID> tags = new HashSet<>();
        tags.add(tagId);

        // When
        Task task = Task.createNew(
                teamId, "Feature", "Description",
                TaskStatus.IN_PROGRESS, TaskPriority.HIGH,
                dueDate, categoryId, assigneeId, tags
        );

        // Then
        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(task.getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(task.getDescription()).isEqualTo("Description");
        assertThat(task.getDueDate()).isEqualTo(dueDate);
        assertThat(task.getAssigneeId()).isEqualTo(assigneeId);
        assertThat(task.getTagIds()).hasSize(1).contains(tagId);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "  ", "AB"})
    @DisplayName("createNew throws exception when title is invalid (null, empty, or too short)")
    void createNew_InvalidTitle_ThrowsException(String invalidTitle) {
        assertThatThrownBy(() -> Task.createNew(teamId, invalidTitle, null, null, null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Task title must be at least 3 characters");
    }

    @Test
    @DisplayName("updateDetails handles non-null values: trims description and creates new tags set")
    void updateDetails_WithValues_Success() {
        // Given
        Task original = Task.createNew(teamId, "Old Title", "", null, null, null, categoryId, null, null);
        UUID tagId = UUID.randomUUID();
        Set<UUID> inputTags = new HashSet<>();
        inputTags.add(tagId);

        // When
        Task updated = original.updateDetails(
                "New Title",
                "   Updated Description   ",
                TaskPriority.HIGH,
                Instant.now(),
                assigneeId,
                categoryId,
                inputTags
        );

        // Then
        assertThat(updated.getDescription()).isEqualTo("Updated Description");
        assertThat(updated.getTagIds()).hasSize(1).contains(tagId);

        assertThat(updated.getTagIds()).isNotSameAs(inputTags);
    }

    @Test
    @DisplayName("updateDetails handles null values: sets empty description and empty tags set")
    void updateDetails_WithNulls_Success() {
        // Given
        Task original = Task.createNew(teamId, "Old Title", "Old Desc", null, null, null, categoryId, null, Set.of(UUID.randomUUID()));

        // When
        Task updated = original.updateDetails(
                "New Title",
                null,
                TaskPriority.LOW,
                Instant.now(),
                assigneeId,
                categoryId,
                null
        );

        // Then
        assertThat(updated.getDescription()).isEmpty();
        assertThat(updated.getDescription()).isNotNull();

        assertThat(updated.getTagIds()).isEmpty();
        assertThat(updated.getTagIds()).isNotNull();
    }

    @Test
    @DisplayName("updateDetails throws exception when new title is invalid")
    void updateDetails_InvalidTitle_ThrowsException() {
        Task task = Task.createNew(teamId, "Valid Title", null, null, null, null, null, null, null);

        assertThatThrownBy(() -> task.updateDetails("AB", null, null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Task title must be at least 3 characters");
    }

    @Test
    @DisplayName("changeStatus updates status and returns new instance")
    void changeStatus_Success() {
        Task task = Task.createNew(teamId, "Task", null, null, null, null, null, null, null);

        Task updated = task.changeStatus(TaskStatus.DONE);

        assertThat(updated.getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(updated).isNotSameAs(task);
    }

    @Test
    @DisplayName("changeStatus throws exception when status is null")
    void changeStatus_Null_ThrowsException() {
        Task task = Task.createNew(teamId, "Task", null, null, null, null, null, null, null);

        assertThatThrownBy(() -> task.changeStatus(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Status cannot be null");
    }

    @Test
    @DisplayName("assign sets assigneeId and returns new instance")
    void assign_Success() {
        Task task = Task.createNew(teamId, "Task", null, null, null, null, null, null, null);

        Task assigned = task.assign(assigneeId);

        assertThat(assigned.getAssigneeId()).isEqualTo(assigneeId);
        assertThat(assigned).isNotSameAs(task);
    }

    @Test
    @DisplayName("unassign removes assigneeId and returns new instance")
    void unassign_Success() {
        Task task = Task.createNew(teamId, "Task", null, null, null, null, null, assigneeId, null);

        Task unassigned = task.unassign();

        assertThat(unassigned.getAssigneeId()).isNull();
        assertThat(unassigned).isNotSameAs(task);
    }
}