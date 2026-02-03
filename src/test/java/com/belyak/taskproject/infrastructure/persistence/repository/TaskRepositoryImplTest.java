package com.belyak.taskproject.infrastructure.persistence.repository;

import com.belyak.taskproject.domain.model.Task;
import com.belyak.taskproject.domain.model.TaskPriority;
import com.belyak.taskproject.domain.model.TaskStatus;
import com.belyak.taskproject.infrastructure.persistence.entity.*;
import com.belyak.taskproject.infrastructure.persistence.mapper.TaskPersistenceMapper;
import com.belyak.taskproject.infrastructure.persistence.projections.TaskInfoProjection;
import com.belyak.taskproject.infrastructure.persistence.repository.impl.TaskRepositoryImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskRepositoryImplTest {

    // ── SUT ─────────────────────────────────────────────────────────────────
    private TaskRepositoryImpl repositoryImpl;

    // ── Collaborators ────────────────────────────────────────────────────────
    @Mock
    private SpringDataTaskRepository     springDataTaskRepository;
    @Mock private SpringDataTeamRepository     teamRepository;
    @Mock private SpringDataUserRepository     userRepository;
    @Mock private SpringDataCategoryRepository categoryRepository;
    @Mock private SpringDataTagRepository      tagRepository;
    @Mock private TaskPersistenceMapper taskPersistenceMapper;

    // ── Reusable fixtures ────────────────────────────────────────────────────
    private static final UUID TASK_ID       = UUID.randomUUID();
    private static final UUID TEAM_ID       = UUID.randomUUID();
    private static final UUID CATEGORY_ID   = UUID.randomUUID();
    private static final UUID ASSIGNEE_ID   = UUID.randomUUID();
    private static final UUID TAG_ID_1      = UUID.randomUUID();
    private static final UUID TAG_ID_2      = UUID.randomUUID();

    private static final Instant EXPECTED_DUE_DATE = LocalDate.of(2026, 3, 1)
            .atStartOfDay(ZoneId.of("UTC")).toInstant();

    private TaskEntity savedEntity;
    private TeamEntity teamProxy;
    private UserEntity assigneeProxy;
    private CategoryEntity categoryProxy;
    private TagEntity tagProxy1, tagProxy2;
    private Task domainTask;

    @BeforeEach
    void setUp() {
        repositoryImpl = new TaskRepositoryImpl(
                springDataTaskRepository,
                teamRepository,
                userRepository,
                categoryRepository,
                tagRepository,
                taskPersistenceMapper
        );

        teamProxy       = new TeamEntity();
        assigneeProxy   = new UserEntity();
        categoryProxy   = new CategoryEntity();
        tagProxy1       = new TagEntity();
        tagProxy2       = new TagEntity();

        savedEntity = new TaskEntity();
        savedEntity.setTags(new HashSet<>());   // mutable set required by .clear()

        domainTask = Task.builder()
                .id(null)
                .teamId(TEAM_ID)
                .title("Write tests")
                .description("Cover every branch")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .dueDate(EXPECTED_DUE_DATE)
                .categoryId(CATEGORY_ID)
                .assigneeId(ASSIGNEE_ID)
                .tagIds(Set.of(TAG_ID_1, TAG_ID_2))
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // save()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("save()")
    class Save {

        // ── NEW entity (id == null) ──────────────────────────────────────────
        @Test
        @DisplayName("New task: creates TaskEntity, sets all fields, saves")
        void whenIdIsNull_createsNewEntityAndSetsAllFields() {
            when(teamRepository.getReferenceById(TEAM_ID))           .thenReturn(teamProxy);
            when(categoryRepository.getReferenceById(CATEGORY_ID))  .thenReturn(categoryProxy);
            when(userRepository.getReferenceById(ASSIGNEE_ID))      .thenReturn(assigneeProxy);
            when(tagRepository.getReferenceById(TAG_ID_1))          .thenReturn(tagProxy1);
            when(tagRepository.getReferenceById(TAG_ID_2))          .thenReturn(tagProxy2);
            when(springDataTaskRepository.save(any(TaskEntity.class))).thenReturn(savedEntity);

            Task expectedDomain = Task.builder().id(TASK_ID).build();
            when(taskPersistenceMapper.toDomain(savedEntity)).thenReturn(expectedDomain);

            // act
            Task result = repositoryImpl.save(domainTask);

            // assert
            assertThat(result).isEqualTo(expectedDomain);

            verify(teamRepository).getReferenceById(TEAM_ID);
            verify(categoryRepository).getReferenceById(CATEGORY_ID);
            verify(userRepository).getReferenceById(ASSIGNEE_ID);
            verify(tagRepository).getReferenceById(TAG_ID_1);
            verify(tagRepository).getReferenceById(TAG_ID_2);

            // entity was persisted
            ArgumentCaptor<TaskEntity> captor = ArgumentCaptor.forClass(TaskEntity.class);
            verify(springDataTaskRepository).save(captor.capture());
            TaskEntity persisted = captor.getValue();

            assertThat(persisted.getTeam())        .isEqualTo(teamProxy);
            assertThat(persisted.getTitle())       .isEqualTo("Write tests");
            assertThat(persisted.getDescription()) .isEqualTo("Cover every branch");
            assertThat(persisted.getStatus())      .isEqualTo(TaskStatus.TODO);
            assertThat(persisted.getPriority())    .isEqualTo(TaskPriority.HIGH);
            assertThat(persisted.getDueDate())     .isEqualTo(EXPECTED_DUE_DATE);
            assertThat(persisted.getCategory())    .isEqualTo(categoryProxy);
            assertThat(persisted.getAssignee())    .isEqualTo(assigneeProxy);
            assertThat(persisted.getTags())        .containsExactlyInAnyOrder(tagProxy1, tagProxy2);
        }

        // ── EXISTING entity (id != null, found) ──────────────────────────────
        @Test
        @DisplayName("Existing task: loads entity by ID, updates fields")
        void whenIdIsNotNull_loadsExistingEntityAndUpdates() {
            Task updateTask = Task.builder()
                    .id(TASK_ID)
                    .teamId(TEAM_ID)
                    .title("Updated title")
                    .description("Updated desc")
                    .status(TaskStatus.IN_PROGRESS)
                    .priority(TaskPriority.LOW)
                    .dueDate(LocalDate.of(2026, 4, 15).atStartOfDay(ZoneId.systemDefault()).toInstant())
                    .categoryId(CATEGORY_ID)
                    .assigneeId(ASSIGNEE_ID)
                    .tagIds(Set.of(TAG_ID_1))
                    .build();

            TaskEntity existingEntity = new TaskEntity();
            existingEntity.setTags(new HashSet<>());

            when(springDataTaskRepository.findById(TASK_ID))        .thenReturn(Optional.of(existingEntity));
            when(categoryRepository.getReferenceById(CATEGORY_ID)) .thenReturn(categoryProxy);
            when(userRepository.getReferenceById(ASSIGNEE_ID))     .thenReturn(assigneeProxy);
            when(tagRepository.getReferenceById(TAG_ID_1))         .thenReturn(tagProxy1);
            when(springDataTaskRepository.save(existingEntity))    .thenReturn(existingEntity);
            when(taskPersistenceMapper.toDomain(existingEntity))   .thenReturn(updateTask);

            Task result = repositoryImpl.save(updateTask);

            assertThat(result).isEqualTo(updateTask);
            assertThat(existingEntity.getTitle())       .isEqualTo("Updated title");
            assertThat(existingEntity.getStatus())      .isEqualTo(TaskStatus.IN_PROGRESS);
            assertThat(existingEntity.getPriority())    .isEqualTo(TaskPriority.LOW);

            // team proxy NOT set again on update (only on create)
            verify(teamRepository, never()).getReferenceById(any());
        }

        // ── EXISTING entity (id != null, NOT found) ──────────────────────────
        @Test
        @DisplayName("Existing task: throws EntityNotFoundException if entity not found")
        void whenIdIsNotNull_andEntityMissing_throwsEntityNotFoundException() {
            Task ghost = Task.builder()
                    .id(TASK_ID)
                    .teamId(TEAM_ID)
                    .title("Ghost")
                    .description("")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.MEDIUM)
                    .dueDate(null)
                    .categoryId(null)
                    .assigneeId(null)
                    .tagIds(Set.of())
                    .build();

            when(springDataTaskRepository.findById(TASK_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> repositoryImpl.save(ghost))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Task not found");
        }

        // ── categoryId == null ────────────────────────────────────────────────
        @Test
        @DisplayName("categoryId == null: does not call getReferenceById for category")
        void whenCategoryIdIsNull_doesNotSetCategory() {
            Task noCategory = Task.builder()
                    .id(null)
                    .teamId(TEAM_ID)
                    .title("No cat")
                    .description("")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.MEDIUM)
                    .dueDate(null)
                    .categoryId(null)       // ← key
                    .assigneeId(ASSIGNEE_ID)
                    .tagIds(Set.of())
                    .build();

            when(teamRepository.getReferenceById(TEAM_ID))      .thenReturn(teamProxy);
            when(userRepository.getReferenceById(ASSIGNEE_ID))  .thenReturn(assigneeProxy);
            when(springDataTaskRepository.save(any()))          .thenReturn(savedEntity);
            when(taskPersistenceMapper.toDomain(savedEntity))   .thenReturn(noCategory);

            repositoryImpl.save(noCategory);

            verify(categoryRepository, never()).getReferenceById(any());
        }

        // ── assigneeId == null ────────────────────────────────────────────────
        @Test
        @DisplayName("assigneeId == null: sets assignee = null (unassign)")
        void whenAssigneeIdIsNull_setsAssigneeNull() {
            Task unassigned = Task.builder()
                    .id(null)
                    .teamId(TEAM_ID)
                    .title("Unassigned")
                    .description("")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.MEDIUM)
                    .dueDate(null)
                    .categoryId(null)
                    .assigneeId(null)       // ← key
                    .tagIds(Set.of())
                    .build();

            when(teamRepository.getReferenceById(TEAM_ID))    .thenReturn(teamProxy);
            when(springDataTaskRepository.save(any()))        .thenReturn(savedEntity);
            when(taskPersistenceMapper.toDomain(savedEntity)).thenReturn(unassigned);

            repositoryImpl.save(unassigned);

            verify(userRepository, never()).getReferenceById(any());

            ArgumentCaptor<TaskEntity> captor = ArgumentCaptor.forClass(TaskEntity.class);
            verify(springDataTaskRepository).save(captor.capture());
            assertThat(captor.getValue().getAssignee()).isNull();
        }

        // ── tagIds == null ────────────────────────────────────────────────────
        @Test
        @DisplayName("tagIds == null: clears tags on entity")
        void whenTagIdsIsNull_clearsTags() {
            Set<TagEntity> existingTags = new HashSet<>(Set.of(tagProxy1));
            TaskEntity entityWithTags   = new TaskEntity();
            entityWithTags.setTags(existingTags);

            Task nullTags = Task.builder()
                    .id(TASK_ID)
                    .teamId(TEAM_ID)
                    .title("Clear tags")
                    .description("")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.MEDIUM)
                    .dueDate(null)
                    .categoryId(null)
                    .assigneeId(null)
                    .tagIds(null)           // ← key
                    .build();

            when(springDataTaskRepository.findById(TASK_ID))     .thenReturn(Optional.of(entityWithTags));
            when(springDataTaskRepository.save(entityWithTags))  .thenReturn(entityWithTags);
            when(taskPersistenceMapper.toDomain(entityWithTags)).thenReturn(nullTags);

            repositoryImpl.save(nullTags);

            assertThat(entityWithTags.getTags()).isEmpty();
            verify(tagRepository, never()).getReferenceById(any());
        }

        // ── tagIds empty set ──────────────────────────────────────────────────
        @Test
        @DisplayName("tagIds == empty set: same as null - clears tags")
        void whenTagIdsIsEmpty_clearsTags() {
            Set<TagEntity> existingTags = new HashSet<>(Set.of(tagProxy1, tagProxy2));
            TaskEntity entityWithTags   = new TaskEntity();
            entityWithTags.setTags(existingTags);

            Task emptyTags = Task.builder()
                    .id(TASK_ID)
                    .teamId(TEAM_ID)
                    .title("Empty tags")
                    .description("")
                    .status(TaskStatus.TODO)
                    .priority(TaskPriority.MEDIUM)
                    .dueDate(null)
                    .categoryId(null)
                    .assigneeId(null)
                    .tagIds(Set.of())       // ← key: empty but not null
                    .build();

            when(springDataTaskRepository.findById(TASK_ID))     .thenReturn(Optional.of(entityWithTags));
            when(springDataTaskRepository.save(entityWithTags))  .thenReturn(entityWithTags);
            when(taskPersistenceMapper.toDomain(entityWithTags)).thenReturn(emptyTags);

            repositoryImpl.save(emptyTags);

            assertThat(entityWithTags.getTags()).isEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // findById()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("Task found: returns Optional with domain model")
        void whenEntityExists_returnsOptionalWithDomain() {
            when(springDataTaskRepository.findById(TASK_ID)).thenReturn(Optional.of(savedEntity));
            when(taskPersistenceMapper.toDomain(savedEntity)).thenReturn(domainTask);

            Optional<Task> result = repositoryImpl.findById(TASK_ID);

            assertThat(result).isPresent().contains(domainTask);
        }

        @Test
        @DisplayName("Task not found: returns Optional.empty()")
        void whenEntityMissing_returnsEmptyOptional() {
            when(springDataTaskRepository.findById(TASK_ID)).thenReturn(Optional.empty());

            Optional<Task> result = repositoryImpl.findById(TASK_ID);

            assertThat(result).isEmpty();
            verify(taskPersistenceMapper, never()).toDomain(any());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // deleteById()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("deleteById()")
    class DeleteById {

        @Test
        @DisplayName("Delegates deletion to SpringData")
        void delegatesToSpringDataRepository() {
            doNothing().when(springDataTaskRepository).deleteById(TASK_ID);

            repositoryImpl.deleteById(TASK_ID);

            verify(springDataTaskRepository).deleteById(TASK_ID);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // findAllByTeamIdAndStatus()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findAllByTeamIdAndStatus()")
    class FindAllByTeamIdAndStatus {

        @Test
        @DisplayName("Returns projection list from SpringData")
        void returnsProjectionList() {
            TaskInfoProjection proj1 = mock(TaskInfoProjection.class);
            TaskInfoProjection proj2 = mock(TaskInfoProjection.class);
            List<TaskInfoProjection> expected = List.of(proj1, proj2);

            when(springDataTaskRepository.findAllByTeamIdAndStatus(TEAM_ID, TaskStatus.TODO))
                    .thenReturn(expected);

            List<TaskInfoProjection> result = repositoryImpl.findAllByTeamIdAndStatus(TEAM_ID, TaskStatus.TODO);

            assertThat(result).hasSize(2).containsExactly(proj1, proj2);
        }

        @Test
        @DisplayName("Empty list: team has no tasks with given status")
        void whenNoMatches_returnsEmptyList() {
            when(springDataTaskRepository.findAllByTeamIdAndStatus(TEAM_ID, TaskStatus.DONE))
                    .thenReturn(Collections.emptyList());

            assertThat(repositoryImpl.findAllByTeamIdAndStatus(TEAM_ID, TaskStatus.DONE)).isEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // findDetailsById()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findDetailsById()")
    class FindDetailsById {

        @Test
        @DisplayName("Task found: Optional with projection")
        void whenExists_returnsOptionalProjection() {
            TaskInfoProjection proj = mock(TaskInfoProjection.class);
            when(springDataTaskRepository.findProjectedById(TASK_ID)).thenReturn(Optional.of(proj));

            assertThat(repositoryImpl.findDetailsById(TASK_ID)).isPresent().contains(proj);
        }

        @Test
        @DisplayName("Task not found: Optional.empty()")
        void whenMissing_returnsEmptyOptional() {
            when(springDataTaskRepository.findProjectedById(TASK_ID)).thenReturn(Optional.empty());

            assertThat(repositoryImpl.findDetailsById(TASK_ID)).isEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // exists…()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("exists…()")
    class Exists {

        @Test
        @DisplayName("existsByCategoryId - true when tasks with category exist")
        void existsByCategoryId_returnsTrue() {
            when(springDataTaskRepository.existsByCategoryId(CATEGORY_ID)).thenReturn(true);
            assertThat(repositoryImpl.existsByCategoryId(CATEGORY_ID)).isTrue();
        }

        @Test
        @DisplayName("existsByCategoryId - false when none exist")
        void existsByCategoryId_returnsFalse() {
            when(springDataTaskRepository.existsByCategoryId(CATEGORY_ID)).thenReturn(false);
            assertThat(repositoryImpl.existsByCategoryId(CATEGORY_ID)).isFalse();
        }

        @Test
        @DisplayName("existsByTagId - true when tasks with tag exist")
        void existsByTagId_returnsTrue() {
            when(springDataTaskRepository.existsByTagsId(TAG_ID_1)).thenReturn(true);
            assertThat(repositoryImpl.existsByTagId(TAG_ID_1)).isTrue();
        }

        @Test
        @DisplayName("existsByTagId - false when none exist")
        void existsByTagId_returnsFalse() {
            when(springDataTaskRepository.existsByTagsId(TAG_ID_1)).thenReturn(false);
            assertThat(repositoryImpl.existsByTagId(TAG_ID_1)).isFalse();
        }

        @Test
        @DisplayName("existsById — true")
        void existsById_returnsTrue() {
            when(springDataTaskRepository.existsById(TASK_ID)).thenReturn(true);
            assertThat(repositoryImpl.existsById(TASK_ID)).isTrue();
        }

        @Test
        @DisplayName("existsById — false")
        void existsById_returnsFalse() {
            when(springDataTaskRepository.existsById(TASK_ID)).thenReturn(false);
            assertThat(repositoryImpl.existsById(TASK_ID)).isFalse();
        }
    }
}