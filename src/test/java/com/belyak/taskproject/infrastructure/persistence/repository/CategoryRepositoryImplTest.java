package com.belyak.taskproject.infrastructure.persistence.repository;

import com.belyak.taskproject.domain.model.Category;
import com.belyak.taskproject.domain.model.TaskStatus;
import com.belyak.taskproject.infrastructure.persistence.entity.CategoryEntity;
import com.belyak.taskproject.infrastructure.persistence.entity.TeamEntity;
import com.belyak.taskproject.infrastructure.persistence.mapper.CategoryPersistenceMapper;
import com.belyak.taskproject.infrastructure.persistence.projections.CategoryInfoWithTaskCountProjection;
import com.belyak.taskproject.infrastructure.persistence.repository.impl.CategoryRepositoryImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryRepositoryImplTest {

    // ── SUT ─────────────────────────────────────────────────────────────────
    private CategoryRepositoryImpl repositoryImpl;

    // ── Collaborators ────────────────────────────────────────────────────────
    @Mock private SpringDataCategoryRepository springDataCategoryRepository;
    @Mock private SpringDataTeamRepository     springDataTeamRepository;
    @Mock private CategoryPersistenceMapper categoryPersistenceMapper;

    // ── Fixtures ─────────────────────────────────────────────────────────────
    private static final UUID   TEAM_ID     = UUID.randomUUID();
    private static final UUID   CATEGORY_ID = UUID.randomUUID();
    private static final UUID   USER_ID     = UUID.randomUUID();
    private static final String CATEGORY_NAME = "Backend";

    private TeamEntity teamProxy;
    private CategoryEntity savedCategoryEntity;
    private Category domainCategory;

    @BeforeEach
    void setUp() {
        repositoryImpl = new CategoryRepositoryImpl(
                springDataCategoryRepository,
                springDataTeamRepository,
                categoryPersistenceMapper
        );

        teamProxy = new TeamEntity();

        savedCategoryEntity = CategoryEntity.builder()
                .id(CATEGORY_ID)
                .name(CATEGORY_NAME)
                .team(teamProxy)
                .isSystem(false)
                .build();

        domainCategory = Category.builder()
                .id(CATEGORY_ID)
                .name(CATEGORY_NAME)
                .isSystem(false)
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // findAllByTeamId()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findAllByTeamId()")
    class FindAllByTeamId {

        @Test
        @DisplayName("Returns category projections with task count")
        void returnsProjectionList() {
            CategoryInfoWithTaskCountProjection p1 = mock(CategoryInfoWithTaskCountProjection.class);
            CategoryInfoWithTaskCountProjection p2 = mock(CategoryInfoWithTaskCountProjection.class);
            when(p1.getName()).thenReturn("Backend");
            when(p2.getName()).thenReturn("Frontend");

            List<CategoryInfoWithTaskCountProjection> expected = List.of(p1, p2);
            when(springDataCategoryRepository.findCategoriesByTeamIdAndStatus(TEAM_ID, TaskStatus.TODO))
                    .thenReturn(expected);

            List<CategoryInfoWithTaskCountProjection> result =
                    repositoryImpl.findAllByTeamId(TEAM_ID, TaskStatus.TODO);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("Backend");
            assertThat(result.get(1).getName()).isEqualTo("Frontend");
        }

        @Test
        @DisplayName("Team with no categories: returns empty list")
        void whenEmpty_returnsEmptyList() {
            when(springDataCategoryRepository.findCategoriesByTeamIdAndStatus(TEAM_ID, TaskStatus.IN_PROGRESS))
                    .thenReturn(Collections.emptyList());

            assertThat(repositoryImpl.findAllByTeamId(TEAM_ID, TaskStatus.IN_PROGRESS)).isEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // createCategory()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("createCategory()")
    class CreateCategory {

        @Test
        @DisplayName("Creates CategoryEntity with team proxy and saves")
        void createsEntityWithTeamProxyAndSaves() {
            when(springDataTeamRepository.getReferenceById(TEAM_ID))               .thenReturn(teamProxy);
            when(springDataCategoryRepository.save(any(CategoryEntity.class)))     .thenReturn(savedCategoryEntity);
            when(categoryPersistenceMapper.toDomain(savedCategoryEntity))          .thenReturn(domainCategory);

            Category input = Category.builder()
                    .id(null)
                    .name(CATEGORY_NAME)
                    .isSystem(false)
                    .build();

            Category result = repositoryImpl.createCategory(TEAM_ID, input);

            assertThat(result).isEqualTo(domainCategory);

            // Verify entity mapping
            ArgumentCaptor<CategoryEntity> captor = ArgumentCaptor.forClass(CategoryEntity.class);
            verify(springDataCategoryRepository).save(captor.capture());
            CategoryEntity persisted = captor.getValue();

            assertThat(persisted.getName())   .isEqualTo(CATEGORY_NAME);
            assertThat(persisted.getTeam())   .isEqualTo(teamProxy);
            assertThat(persisted.isSystem())  .isFalse();
        }

        @Test
        @DisplayName("System category: isSystem flag preserved")
        void whenSystemCategory_isSytemIsTrue() {
            Category systemCat = Category.builder()
                    .id(null)
                    .name("System")
                    .isSystem(true)
                    .build();

            when(springDataTeamRepository.getReferenceById(TEAM_ID))             .thenReturn(teamProxy);
            when(springDataCategoryRepository.save(any(CategoryEntity.class)))   .thenReturn(savedCategoryEntity);
            when(categoryPersistenceMapper.toDomain(savedCategoryEntity))        .thenReturn(systemCat);

            repositoryImpl.createCategory(TEAM_ID, systemCat);

            ArgumentCaptor<CategoryEntity> captor = ArgumentCaptor.forClass(CategoryEntity.class);
            verify(springDataCategoryRepository).save(captor.capture());
            assertThat(captor.getValue().isSystem()).isTrue();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // existsByName()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("existsByName()")
    class ExistsByName {

        @Test
        @DisplayName("Name taken -> true")
        void whenNameTaken_returnsTrue() {
            when(springDataCategoryRepository.existsByTeamIdAndNameIgnoreCase(TEAM_ID, CATEGORY_NAME))
                    .thenReturn(true);

            assertThat(repositoryImpl.existsByName(TEAM_ID, CATEGORY_NAME)).isTrue();
        }

        @Test
        @DisplayName("Name free -> false")
        void whenNameFree_returnsFalse() {
            when(springDataCategoryRepository.existsByTeamIdAndNameIgnoreCase(TEAM_ID, CATEGORY_NAME))
                    .thenReturn(false);

            assertThat(repositoryImpl.existsByName(TEAM_ID, CATEGORY_NAME)).isFalse();
        }

        @Test
        @DisplayName("Case-insensitive check: 'backend' matches 'Backend'")
        void caseInsensitiveCheck() {
            when(springDataCategoryRepository.existsByTeamIdAndNameIgnoreCase(TEAM_ID, "backend"))
                    .thenReturn(true);

            assertThat(repositoryImpl.existsByName(TEAM_ID, "backend")).isTrue();
            verify(springDataCategoryRepository).existsByTeamIdAndNameIgnoreCase(TEAM_ID, "backend");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // findById()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("Found: Optional with domain model")
        void whenFound_returnsOptional() {
            when(springDataCategoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(savedCategoryEntity));
            when(categoryPersistenceMapper.toDomain(savedCategoryEntity)).thenReturn(domainCategory);

            assertThat(repositoryImpl.findById(CATEGORY_ID)).isPresent().contains(domainCategory);
        }

        @Test
        @DisplayName("Not found: Optional.empty()")
        void whenMissing_returnsEmpty() {
            when(springDataCategoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

            assertThat(repositoryImpl.findById(CATEGORY_ID)).isEmpty();
            verify(categoryPersistenceMapper, never()).toDomain(any());
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
            doNothing().when(springDataCategoryRepository).deleteById(CATEGORY_ID);

            repositoryImpl.deleteById(CATEGORY_ID);

            verify(springDataCategoryRepository).deleteById(CATEGORY_ID);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // canAccess()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("canAccess()")
    class CanAccess {

        @Test
        @DisplayName("User is member or owner of team -> true")
        void whenUserHasAccess_returnsTrue() {
            when(springDataCategoryRepository.canAccess(CATEGORY_ID, USER_ID)).thenReturn(true);
            assertThat(repositoryImpl.canAccess(CATEGORY_ID, USER_ID)).isTrue();
        }

        @Test
        @DisplayName("User not linked to team -> false")
        void whenUserHasNoAccess_returnsFalse() {
            when(springDataCategoryRepository.canAccess(CATEGORY_ID, USER_ID)).thenReturn(false);
            assertThat(repositoryImpl.canAccess(CATEGORY_ID, USER_ID)).isFalse();
        }
    }
}