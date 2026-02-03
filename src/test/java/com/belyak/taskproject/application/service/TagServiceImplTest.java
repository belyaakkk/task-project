package com.belyak.taskproject.application.service;

import com.belyak.taskproject.domain.exception.TagAlreadyExistsException;
import com.belyak.taskproject.domain.model.Tag;
import com.belyak.taskproject.domain.model.TagConstants;
import com.belyak.taskproject.domain.model.TaskStatus;
import com.belyak.taskproject.domain.policy.TagDeletionRule;
import com.belyak.taskproject.domain.port.repository.TagRepository;
import com.belyak.taskproject.domain.port.repository.TeamRepository;
import com.belyak.taskproject.infrastructure.persistence.projections.TagInfoWithTaskCountProjection;
import com.belyak.taskproject.web.dto.request.CreateTagsRequest;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TagServiceImpl Unit Tests")
class TagServiceImplTest {

    // ── SUT ─────────────────────────────────────────────────────────────────
    @InjectMocks
    private TagServiceImpl tagService;

    // ── Collaborators ────────────────────────────────────────────────────────
    @Mock private TagRepository tagRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private TagDeletionRule deletionRule;

    // ── Captors ──────────────────────────────────────────────────────────────
    @Captor
    private ArgumentCaptor<List<Tag>> tagsCaptor;

    // ── Fixtures ─────────────────────────────────────────────────────────────
    private static final UUID TEAM_ID = UUID.randomUUID();
    private static final UUID TAG_ID  = UUID.randomUUID();

    private Tag tag;

    @BeforeEach
    void setUp() {
        // Explicitly inject deletion rules list (as @InjectMocks might fail with Lists sometimes)
        tagService = new TagServiceImpl(tagRepository, teamRepository, List.of(deletionRule));

        tag = Tag.builder()
                .id(TAG_ID)
                .name("Urgent")
                .color("#FF0000")
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // findTeamTags()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findTeamTags()")
    class FindTeamTags {

        @Test
        @DisplayName("Returns list of tag projections")
        void shouldFindTeamTags() {
            TagInfoWithTaskCountProjection projection = mock(TagInfoWithTaskCountProjection.class);
            when(tagRepository.findAllByTeamId(eq(TEAM_ID), eq(TaskStatus.IN_PROGRESS)))
                    .thenReturn(List.of(projection));

            List<TagInfoWithTaskCountProjection> result = tagService.findTeamTags(TEAM_ID);

            assertThat(result).hasSize(1).contains(projection);
            verify(tagRepository).findAllByTeamId(TEAM_ID, TaskStatus.IN_PROGRESS);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // createTags()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("createTags()")
    class CreateTags {

        @Test
        @DisplayName("Valid request: creates tags with custom colors")
        void shouldCreateTagsWithCustomColors() {
            // Given
            CreateTagsRequest request = CreateTagsRequest.builder()
                    .tags(Set.of(
                            new CreateTagsRequest.TagItem("Urgent", "#FF0000"),
                            new CreateTagsRequest.TagItem("Low", "#00FF00")
                    ))
                    .build();

            when(teamRepository.existsById(TEAM_ID)).thenReturn(true);
            when(tagRepository.findExistingTagNames(any(), any())).thenReturn(Set.of()); // No duplicates in DB

            Tag savedTag1 = Tag.builder().name("Urgent").color("#FF0000").build();
            Tag savedTag2 = Tag.builder().name("Low").color("#00FF00").build();
            when(tagRepository.createTags(eq(TEAM_ID), any())).thenReturn(List.of(savedTag1, savedTag2));

            // When
            List<Tag> result = tagService.createTags(TEAM_ID, request);

            // Then
            assertThat(result).hasSize(2);
            verify(tagRepository).createTags(eq(TEAM_ID), tagsCaptor.capture());

            List<Tag> captured = tagsCaptor.getValue();
            assertThat(captured).hasSize(2);
            assertThat(captured).extracting(Tag::getName)
                    .containsExactlyInAnyOrder("Urgent", "Low");
            assertThat(captured).extracting(Tag::getColor)
                    .containsExactlyInAnyOrder("#FF0000", "#00FF00");
        }

        @Test
        @DisplayName("Missing color: uses default color")
        void shouldCreateTagsWithDefaultColor() {
            CreateTagsRequest request = CreateTagsRequest.builder()
                    .tags(Set.of(new CreateTagsRequest.TagItem("Bug", null)))
                    .build();

            when(teamRepository.existsById(TEAM_ID)).thenReturn(true);
            when(tagRepository.findExistingTagNames(any(), any())).thenReturn(Set.of());
            when(tagRepository.createTags(any(), any())).thenReturn(List.of());

            tagService.createTags(TEAM_ID, request);

            verify(tagRepository).createTags(eq(TEAM_ID), tagsCaptor.capture());
            Tag captured = tagsCaptor.getValue().get(0);

            assertThat(captured.getColor()).isEqualTo(TagConstants.DEFAULT_COLOR);
        }

        @Test
        @DisplayName("Trims whitespace from tag names")
        void shouldTrimTagNames() {
            CreateTagsRequest request = CreateTagsRequest.builder()
                    .tags(Set.of(new CreateTagsRequest.TagItem("  Space  ", null)))
                    .build();

            when(teamRepository.existsById(TEAM_ID)).thenReturn(true);
            when(tagRepository.findExistingTagNames(any(), any())).thenReturn(Set.of());

            tagService.createTags(TEAM_ID, request);

            verify(tagRepository).createTags(eq(TEAM_ID), tagsCaptor.capture());
            assertThat(tagsCaptor.getValue().get(0).getName()).isEqualTo("Space");
        }

        @Test
        @DisplayName("Team missing: throws EntityNotFoundException")
        void shouldThrowExceptionWhenTeamNotExists() {
            CreateTagsRequest request = CreateTagsRequest.builder().tags(Set.of()).build();
            when(teamRepository.existsById(TEAM_ID)).thenReturn(false);

            assertThatThrownBy(() -> tagService.createTags(TEAM_ID, request))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Team not found");

            verify(tagRepository, never()).createTags(any(), any());
        }

        @Test
        @DisplayName("Duplicate names in request: throws IllegalArgumentException")
        void shouldThrowExceptionWhenDuplicateTagNamesInRequest() {
            // Note: Set allows this if objects are distinct (e.g. different colors),
            // but service must catch logic duplication (case-insensitive name)
            CreateTagsRequest request = CreateTagsRequest.builder()
                    .tags(Set.of(
                            new CreateTagsRequest.TagItem("urgent", "#FF0000"),
                            new CreateTagsRequest.TagItem("URGENT", "#0000FF")
                    ))
                    .build();

            when(teamRepository.existsById(TEAM_ID)).thenReturn(true);

            assertThatThrownBy(() -> tagService.createTags(TEAM_ID, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Duplicate tag names"); // Check service message
        }

        @Test
        @DisplayName("Tag exists in DB: throws TagAlreadyExistsException")
        void shouldThrowExceptionWhenTagAlreadyExists() {
            CreateTagsRequest request = CreateTagsRequest.builder()
                    .tags(Set.of(new CreateTagsRequest.TagItem("Urgent", null)))
                    .build();

            when(teamRepository.existsById(TEAM_ID)).thenReturn(true);
            // Simulate that "urgent" already exists in DB
            when(tagRepository.findExistingTagNames(any(), any())).thenReturn(Set.of("urgent"));

            assertThatThrownBy(() -> tagService.createTags(TEAM_ID, request))
                    .isInstanceOf(TagAlreadyExistsException.class)
                    .hasMessageContaining("Tags already exist");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // deleteTag()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("deleteTag()")
    class DeleteTag {

        @Test
        @DisplayName("Tag exists and valid: deletes tag")
        void shouldDeleteTag() {
            when(tagRepository.findById(TAG_ID)).thenReturn(Optional.of(tag));
            doNothing().when(deletionRule).validate(tag);

            tagService.deleteTag(TAG_ID);

            verify(deletionRule).validate(tag);
            verify(tagRepository).deleteById(TAG_ID);
        }

        @Test
        @DisplayName("Tag missing: throws EntityNotFoundException")
        void shouldThrowExceptionWhenDeletingNonExistentTag() {
            when(tagRepository.findById(TAG_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> tagService.deleteTag(TAG_ID))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Tag with id");

            verify(tagRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Rule violation: throws exception and aborts delete")
        void whenRuleFails_throwsException() {
            when(tagRepository.findById(TAG_ID)).thenReturn(Optional.of(tag));
            doThrow(new RuntimeException("Rule failed")).when(deletionRule).validate(tag);

            assertThatThrownBy(() -> tagService.deleteTag(TAG_ID))
                    .isInstanceOf(RuntimeException.class);

            verify(tagRepository, never()).deleteById(any());
        }
    }
}