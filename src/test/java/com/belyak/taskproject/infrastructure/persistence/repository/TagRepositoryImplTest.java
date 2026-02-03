package com.belyak.taskproject.infrastructure.persistence.repository;

import com.belyak.taskproject.domain.model.TaskStatus;
import com.belyak.taskproject.infrastructure.persistence.entity.TagEntity;
import com.belyak.taskproject.infrastructure.persistence.entity.TeamEntity;
import com.belyak.taskproject.infrastructure.persistence.mapper.TagPersistenceMapper;
import com.belyak.taskproject.infrastructure.persistence.projections.TagInfoWithTaskCountProjection;
import com.belyak.taskproject.infrastructure.persistence.repository.impl.TagRepositoryImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import com.belyak.taskproject.domain.model.Tag;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagRepositoryImplTest {

    // ── SUT ─────────────────────────────────────────────────────────────────
    private TagRepositoryImpl repositoryImpl;

    // ── Collaborators ────────────────────────────────────────────────────────
    @Mock private SpringDataTagRepository  springDataTagRepository;
    @Mock private SpringDataTeamRepository springDataTeamRepository;
    @Mock private TagPersistenceMapper tagPersistenceMapper;

    // ── Fixtures ─────────────────────────────────────────────────────────────
    private static final UUID   TEAM_ID = UUID.randomUUID();
    private static final UUID   TAG_ID  = UUID.randomUUID();
    private static final UUID   USER_ID = UUID.randomUUID();

    private TeamEntity teamProxy;
    private TagEntity  savedEntity1, savedEntity2;
    private Tag        domainTag1, domainTag2;

    @BeforeEach
    void setUp() {
        repositoryImpl = new TagRepositoryImpl(
                springDataTagRepository,
                springDataTeamRepository,
                tagPersistenceMapper
        );

        teamProxy     = new TeamEntity();
        savedEntity1  = new TagEntity();
        savedEntity2  = new TagEntity();

        domainTag1 = Tag.builder().id(UUID.randomUUID()).name("urgent").color("#FF0000").build();
        domainTag2 = Tag.builder().id(UUID.randomUUID()).name("docs").color("#00FF00").build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // findAllByTeamId()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findAllByTeamId()")
    class FindAllByTeamId {

        @Test
        @DisplayName("Returns tag projections with task counts")
        void returnsProjectionList() {
            TagInfoWithTaskCountProjection p1 = mock(TagInfoWithTaskCountProjection.class);
            TagInfoWithTaskCountProjection p2 = mock(TagInfoWithTaskCountProjection.class);
            when(p1.getName()).thenReturn("urgent");
            when(p2.getName()).thenReturn("docs");

            when(springDataTagRepository.findTagsByTeamIdAndStatus(TEAM_ID, TaskStatus.TODO))
                    .thenReturn(List.of(p1, p2));

            List<TagInfoWithTaskCountProjection> result =
                    repositoryImpl.findAllByTeamId(TEAM_ID, TaskStatus.TODO);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("urgent");
            assertThat(result.get(1).getName()).isEqualTo("docs");
        }

        @Test
        @DisplayName("No tags found: returns empty list")
        void whenEmpty_returnsEmptyList() {
            when(springDataTagRepository.findTagsByTeamIdAndStatus(TEAM_ID, TaskStatus.DONE))
                    .thenReturn(Collections.emptyList());

            assertThat(repositoryImpl.findAllByTeamId(TEAM_ID, TaskStatus.DONE)).isEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // createTags()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("createTags()")
    class CreateTags {

        @Test
        @DisplayName("Batch creation: map -> entity -> set team proxy -> saveAll -> map back")
        void createsMultipleTagsInBatch() {
            TagEntity mapped1 = new TagEntity();
            TagEntity mapped2 = new TagEntity();

            when(springDataTeamRepository.getReferenceById(TEAM_ID))  .thenReturn(teamProxy);
            when(tagPersistenceMapper.toEntity(domainTag1))           .thenReturn(mapped1);
            when(tagPersistenceMapper.toEntity(domainTag2))           .thenReturn(mapped2);
            when(springDataTagRepository.saveAll(List.of(mapped1, mapped2)))
                    .thenReturn(List.of(savedEntity1, savedEntity2));
            when(tagPersistenceMapper.toDomain(savedEntity1))        .thenReturn(domainTag1);
            when(tagPersistenceMapper.toDomain(savedEntity2))        .thenReturn(domainTag2);

            List<Tag> result = repositoryImpl.createTags(TEAM_ID, List.of(domainTag1, domainTag2));

            assertThat(result).hasSize(2).containsExactly(domainTag1, domainTag2);

            // Verify team proxy set on each entity
            assertThat(mapped1.getTeam()).isEqualTo(teamProxy);
            assertThat(mapped2.getTeam()).isEqualTo(teamProxy);

            verify(springDataTeamRepository).getReferenceById(TEAM_ID);
            verify(springDataTagRepository).saveAll(List.of(mapped1, mapped2));
        }

        @Test
        @DisplayName("Single tag creation: works correctly through batch path")
        void createsSingleTag() {
            TagEntity mapped = new TagEntity();

            when(springDataTeamRepository.getReferenceById(TEAM_ID)).thenReturn(teamProxy);
            when(tagPersistenceMapper.toEntity(domainTag1))         .thenReturn(mapped);
            when(springDataTagRepository.saveAll(List.of(mapped)))  .thenReturn(List.of(savedEntity1));
            when(tagPersistenceMapper.toDomain(savedEntity1))       .thenReturn(domainTag1);

            List<Tag> result = repositoryImpl.createTags(TEAM_ID, List.of(domainTag1));

            assertThat(result).hasSize(1).contains(domainTag1);
            assertThat(mapped.getTeam()).isEqualTo(teamProxy);
        }

        @Test
        @DisplayName("Empty list input: saveAll called with empty list, returns empty")
        void whenEmptyList_saveAllCalledWithEmpty() {
            when(springDataTeamRepository.getReferenceById(TEAM_ID)).thenReturn(teamProxy);
            when(springDataTagRepository.saveAll(Collections.emptyList()))
                    .thenReturn(Collections.emptyList());

            List<Tag> result = repositoryImpl.createTags(TEAM_ID, Collections.emptyList());

            assertThat(result).isEmpty();
            verify(springDataTagRepository).saveAll(Collections.emptyList());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // canAccess()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("canAccess()")
    class CanAccess {

        @Test
        @DisplayName("User is member of tag's team -> true")
        void whenMember_returnsTrue() {
            when(springDataTagRepository.canAccess(TAG_ID, USER_ID)).thenReturn(true);
            assertThat(repositoryImpl.canAccess(TAG_ID, USER_ID)).isTrue();
        }

        @Test
        @DisplayName("User not linked to tag -> false")
        void whenNotMember_returnsFalse() {
            when(springDataTagRepository.canAccess(TAG_ID, USER_ID)).thenReturn(false);
            assertThat(repositoryImpl.canAccess(TAG_ID, USER_ID)).isFalse();
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
            doNothing().when(springDataTagRepository).deleteById(TAG_ID);

            repositoryImpl.deleteById(TAG_ID);

            verify(springDataTagRepository).deleteById(TAG_ID);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // findById()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("Tag found: returns Optional with domain model")
        void whenFound_returnsOptional() {
            when(springDataTagRepository.findById(TAG_ID)).thenReturn(Optional.of(savedEntity1));
            when(tagPersistenceMapper.toDomain(savedEntity1)).thenReturn(domainTag1);

            assertThat(repositoryImpl.findById(TAG_ID)).isPresent().contains(domainTag1);
        }

        @Test
        @DisplayName("Tag not found: returns Optional.empty()")
        void whenMissing_returnsEmpty() {
            when(springDataTagRepository.findById(TAG_ID)).thenReturn(Optional.empty());

            assertThat(repositoryImpl.findById(TAG_ID)).isEmpty();
            verify(tagPersistenceMapper, never()).toDomain(any());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // findExistingTagNames()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findExistingTagNames()")
    class FindExistingTagNames {

        @Test
        @DisplayName("Some names exist: returns intersection")
        void whenSomeExist_returnsExisting() {
            Set<String> input    = Set.of("Urgent", "Docs", "NewTag");
            Set<String> existing = Set.of("urgent", "docs");   // from DB lowercase

            when(springDataTagRepository.findNamesByTeamIdAndNamesIn(TEAM_ID, Set.of("urgent", "docs", "newtag")))
                    .thenReturn(existing);

            Set<String> result = repositoryImpl.findExistingTagNames(TEAM_ID, input);

            assertThat(result).containsExactlyInAnyOrder("urgent", "docs");
            // verify lowercase names passed to repository
            verify(springDataTagRepository).findNamesByTeamIdAndNamesIn(
                    TEAM_ID, Set.of("urgent", "docs", "newtag")
            );
        }

        @Test
        @DisplayName("Empty input: returns empty set without DB call")
        void whenInputEmpty_returnsEmptyWithoutDbCall() {
            Set<String> result = repositoryImpl.findExistingTagNames(TEAM_ID, Set.of());

            assertThat(result).isEmpty();
            verify(springDataTagRepository, never()).findNamesByTeamIdAndNamesIn(any(), any());
        }

        @Test
        @DisplayName("No matches found: returns empty set")
        void whenNoMatches_returnsEmptySet() {
            Set<String> input = Set.of("BrandNew");

            when(springDataTagRepository.findNamesByTeamIdAndNamesIn(TEAM_ID, Set.of("brandnew")))
                    .thenReturn(Collections.emptySet());

            assertThat(repositoryImpl.findExistingTagNames(TEAM_ID, input)).isEmpty();
        }

        @Test
        @DisplayName("All names exist: returns all")
        void whenAllExist_returnsAll() {
            Set<String> input    = Set.of("Urgent", "Docs");
            Set<String> existing = Set.of("urgent", "docs");

            when(springDataTagRepository.findNamesByTeamIdAndNamesIn(TEAM_ID, Set.of("urgent", "docs")))
                    .thenReturn(existing);

            assertThat(repositoryImpl.findExistingTagNames(TEAM_ID, input))
                    .containsExactlyInAnyOrder("urgent", "docs");
        }
    }
}