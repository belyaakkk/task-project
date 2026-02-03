package com.belyak.taskproject.application.service;

import com.belyak.taskproject.domain.model.Task;
import com.belyak.taskproject.domain.model.TaskPriority;
import com.belyak.taskproject.domain.model.TaskStatus;
import com.belyak.taskproject.domain.port.repository.TaskRepository;
import com.belyak.taskproject.domain.port.repository.TeamRepository;
import com.belyak.taskproject.infrastructure.persistence.projections.TaskInfoProjection;
import com.belyak.taskproject.web.dto.request.CreateTaskRequest;
import com.belyak.taskproject.web.dto.request.UpdateTaskRequest;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskServiceImpl Unit Tests")
class TaskServiceImplTest {

    // ── SUT ─────────────────────────────────────────────────────────────────
    @InjectMocks
    private TaskServiceImpl taskService;

    // ── Collaborators ────────────────────────────────────────────────────────
    @Mock private TaskRepository taskRepository;
    @Mock private TeamRepository teamRepository;

    // ── Captors ──────────────────────────────────────────────────────────────
    @Captor
    private ArgumentCaptor<Task> taskCaptor;

    // ── Fixtures ─────────────────────────────────────────────────────────────
    private static final UUID TEAM_ID = UUID.randomUUID();
    private static final UUID TASK_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID CATEGORY_ID = UUID.randomUUID();

    private Task existingTask;

    @BeforeEach
    void setUp() {
        existingTask = Task.builder()
                .id(TASK_ID)
                .teamId(TEAM_ID)
                .title("Existing Task")
                .description("Desc")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .assigneeId(USER_ID)
                .categoryId(CATEGORY_ID)
                .tagIds(Set.of())
                .dueDate(Instant.now().plus(7, ChronoUnit.DAYS))
                .createdAt(Instant.now())
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // findTeamTasks()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findTeamTasks()")
    class FindTeamTasks {

        @Test
        @DisplayName("Returns list of task projections")
        void shouldFindTeamTasks() {
            TaskInfoProjection projection = mock(TaskInfoProjection.class);
            when(taskRepository.findAllByTeamIdAndStatus(TEAM_ID, TaskStatus.IN_PROGRESS))
                    .thenReturn(List.of(projection));

            List<TaskInfoProjection> result = taskService.findTeamTasks(TEAM_ID, TaskStatus.IN_PROGRESS);

            assertThat(result).hasSize(1).contains(projection);
            verify(taskRepository).findAllByTeamIdAndStatus(TEAM_ID, TaskStatus.IN_PROGRESS);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // getTaskDetails()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getTaskDetails()")
    class GetTaskDetails {

        @Test
        @DisplayName("Task found: returns details")
        void shouldGetTaskDetails() {
            TaskInfoProjection projection = mock(TaskInfoProjection.class);
            when(taskRepository.findDetailsById(TASK_ID)).thenReturn(Optional.of(projection));

            TaskInfoProjection result = taskService.getTaskDetails(TEAM_ID, TASK_ID);

            assertThat(result).isEqualTo(projection);
        }

        @Test
        @DisplayName("Task not found: throws EntityNotFoundException")
        void shouldThrowExceptionWhenTaskNotFound() {
            when(taskRepository.findDetailsById(TASK_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.getTaskDetails(TEAM_ID, TASK_ID))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Task with id");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // createTask()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("createTask()")
    class CreateTask {

        @Test
        @DisplayName("Valid request with assignee: verifies membership and saves")
        void shouldCreateTaskWithAllFields() {
            // Given
            CreateTaskRequest request = CreateTaskRequest.builder()
                    .title("New Task")
                    .status(TaskStatus.TODO)
                    .assigneeId(USER_ID)
                    .build();

            when(teamRepository.isMember(TEAM_ID, USER_ID)).thenReturn(true);
            when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            Task result = taskService.createTask(TEAM_ID, request);

            // Then
            verify(teamRepository).isMember(TEAM_ID, USER_ID);
            verify(taskRepository).save(taskCaptor.capture());

            Task captured = taskCaptor.getValue();
            assertThat(captured.getTitle()).isEqualTo("New Task");
            assertThat(captured.getTeamId()).isEqualTo(TEAM_ID);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Request without status/priority: applies defaults (DRAFT/MEDIUM)")
        void shouldCreateTaskWithDefaults() {
            CreateTaskRequest request = CreateTaskRequest.builder()
                    .title("Draft Task")
                    .build();

            when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

            Task result = taskService.createTask(TEAM_ID, request);

            assertThat(result.getStatus()).isEqualTo(TaskStatus.DRAFT);
            assertThat(result.getPriority()).isEqualTo(TaskPriority.MEDIUM);
        }

        @Test
        @DisplayName("Assignee not in team: throws IllegalArgumentException")
        void shouldThrowExceptionWhenAssigneeNotTeamMember() {
            CreateTaskRequest request = CreateTaskRequest.builder()
                    .title("Task")
                    .assigneeId(USER_ID)
                    .build();

            when(teamRepository.isMember(TEAM_ID, USER_ID)).thenReturn(false);

            assertThatThrownBy(() -> taskService.createTask(TEAM_ID, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Assignee must be a member");

            verify(taskRepository, never()).save(any());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // updateTask()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("updateTask()")
    class UpdateTask {

        @Test
        @DisplayName("Task exists and belongs to team: updates fields")
        void shouldUpdateTask() {
            UpdateTaskRequest request = UpdateTaskRequest.builder()
                    .title("Updated Title")
                    .priority(TaskPriority.URGENT)
                    .build();

            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(existingTask));
            when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

            Task result = taskService.updateTask(TEAM_ID, TASK_ID, request);

            assertThat(result.getTitle()).isEqualTo("Updated Title");
            assertThat(result.getPriority()).isEqualTo(TaskPriority.URGENT);
        }

        @Test
        @DisplayName("Task from different team: throws AccessDeniedException")
        void shouldThrowExceptionWhenUpdatingTaskFromDifferentTeam() {
            UUID differentTeamId = UUID.randomUUID();
            UpdateTaskRequest request = UpdateTaskRequest.builder().build();

            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(existingTask));

            assertThatThrownBy(() -> taskService.updateTask(differentTeamId, TASK_ID, request))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Task does not belong");

            verify(taskRepository, never()).save(any());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // updateStatus()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("updateStatus()")
    class UpdateStatus {

        @Test
        @DisplayName("Updates status successfully")
        void shouldUpdateTaskStatus() {
            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(existingTask));
            when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

            taskService.updateStatus(TEAM_ID, TASK_ID, TaskStatus.DONE);

            verify(taskRepository).save(taskCaptor.capture());
            assertThat(taskCaptor.getValue().getStatus()).isEqualTo(TaskStatus.DONE);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // assignTask()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("assignTask()")
    class AssignTask {

        @Test
        @DisplayName("Valid user: assigns task")
        void shouldAssignTask() {
            UUID newAssigneeId = UUID.randomUUID();
            when(teamRepository.isMember(TEAM_ID, newAssigneeId)).thenReturn(true);
            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(existingTask));
            when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

            taskService.assignTask(TEAM_ID, TASK_ID, newAssigneeId);

            verify(taskRepository).save(taskCaptor.capture());
            assertThat(taskCaptor.getValue().getAssigneeId()).isEqualTo(newAssigneeId);
        }

        @Test
        @DisplayName("Null user: unassigns task (member check skipped)")
        void shouldUnassignTask() {
            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(existingTask));
            when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

            taskService.assignTask(TEAM_ID, TASK_ID, null);

            verify(teamRepository, never()).isMember(any(), any());
            verify(taskRepository).save(taskCaptor.capture());
            assertThat(taskCaptor.getValue().getAssigneeId()).isNull();
        }

        @Test
        @DisplayName("Non-member: throws IllegalArgumentException")
        void shouldThrowExceptionWhenAssigningToNonTeamMember() {
            UUID outsiderId = UUID.randomUUID();

            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(existingTask));

            when(teamRepository.isMember(TEAM_ID, outsiderId)).thenReturn(false);

            assertThatThrownBy(() -> taskService.assignTask(TEAM_ID, TASK_ID, outsiderId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Assignee must be a member");
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // deleteTask()
        // ═══════════════════════════════════════════════════════════════════════════

        @Nested
        @DisplayName("deleteTask()")
        class DeleteTask {

            @Test
            @DisplayName("Task exists: deletes it")
            void shouldDeleteTask() {
                when(taskRepository.existsById(TASK_ID)).thenReturn(true);

                taskService.deleteTask(TEAM_ID, TASK_ID);

                verify(taskRepository).deleteById(TASK_ID);
            }

            @Test
            @DisplayName("Task missing: throws EntityNotFoundException")
            void shouldThrowExceptionWhenDeletingNonExistentTask() {
                when(taskRepository.existsById(TASK_ID)).thenReturn(false);

                assertThatThrownBy(() -> taskService.deleteTask(TEAM_ID, TASK_ID))
                        .isInstanceOf(EntityNotFoundException.class);

                verify(taskRepository, never()).deleteById(any());
            }
        }
    }
}