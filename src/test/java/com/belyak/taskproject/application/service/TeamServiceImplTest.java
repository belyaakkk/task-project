package com.belyak.taskproject.application.service;

import com.belyak.taskproject.application.mapper.TeamApiMapper;
import com.belyak.taskproject.common.util.UniqueCodeGenerator;
import com.belyak.taskproject.domain.model.Team;
import com.belyak.taskproject.domain.model.TeamStatus;
import com.belyak.taskproject.domain.port.repository.TeamRepository;
import com.belyak.taskproject.infrastructure.persistence.projections.TeamDetailsProjection;
import com.belyak.taskproject.infrastructure.persistence.projections.TeamSummaryProjection;
import com.belyak.taskproject.web.dto.request.CreateTeamRequest;
import com.belyak.taskproject.web.dto.request.JoinTeamRequest;
import com.belyak.taskproject.web.dto.response.TeamDetailsResponse;
import com.belyak.taskproject.web.dto.response.TeamResponse;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TeamService Unit Tests")
class TeamServiceImplTest {

    // ── SUT ─────────────────────────────────────────────────────────────────
    @InjectMocks
    private TeamServiceImpl teamService;

    // ── Collaborators ────────────────────────────────────────────────────────
    @Mock private TeamRepository teamRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UniqueCodeGenerator uniqueCodeGenerator;
    @Mock private TeamApiMapper teamApiMapper;

    // ── Captors ──────────────────────────────────────────────────────────────
    @Captor
    private ArgumentCaptor<Team> teamCaptor;

    // ── Fixtures ─────────────────────────────────────────────────────────────
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID TEAM_ID = UUID.randomUUID();
    private static final String JOIN_CODE = "ABC123";
    private static final String PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "encodedPassword";

    private Team team;

    @BeforeEach
    void setUp() {
        team = Team.builder()
                .id(TEAM_ID)
                .name("Development Team")
                .joinCode(JOIN_CODE)
                .password(ENCODED_PASSWORD)
                .ownerId(USER_ID)
                .status(TeamStatus.ACTIVE)
                .memberIds(new HashSet<>(Set.of(USER_ID)))
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // getTeamsByMemberId()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getTeamsByMemberId()")
    class GetTeamsByMemberId {

        @Test
        @DisplayName("Returns team projections for user")
        void shouldGetTeamsByMemberId() {
            TeamSummaryProjection projection = mock(TeamSummaryProjection.class);
            when(teamRepository.getAllByMemberId(USER_ID)).thenReturn(List.of(projection));

            List<TeamSummaryProjection> result = teamService.getTeamsByMemberId(USER_ID);

            assertThat(result).hasSize(1).contains(projection);
            verify(teamRepository).getAllByMemberId(USER_ID);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // createTeam()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("createTeam()")
    class CreateTeam {

        @Test
        @DisplayName("Valid request: generates code, encodes password, saves team")
        void shouldCreateTeam() {
            CreateTeamRequest request = CreateTeamRequest.builder()
                    .name("Development Team")
                    .password(PASSWORD)
                    .build();

            when(uniqueCodeGenerator.generate(any(), any())).thenAnswer(invocation -> {
                Supplier<String> supplier = invocation.getArgument(0);

                supplier.get();

                return JOIN_CODE;
            });

            when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(teamRepository.save(any(Team.class))).thenReturn(team);

            Team result = teamService.createTeam(request, USER_ID);

            assertThat(result).isNotNull();

            verify(uniqueCodeGenerator).generate(any(), any());
            verify(passwordEncoder).encode(PASSWORD);
            verify(teamRepository).save(teamCaptor.capture());

            Team savedTeam = teamCaptor.getValue();
            assertThat(savedTeam.getJoinCode()).isEqualTo(JOIN_CODE);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // joinTeam()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("joinTeam()")
    class JoinTeam {

        @Test
        @DisplayName("Valid code & password: adds member and returns response")
        void shouldJoinTeam() {
            UUID newUserId = UUID.randomUUID();
            JoinTeamRequest request = new JoinTeamRequest(JOIN_CODE, PASSWORD);

            TeamResponse responseDTO = TeamResponse.builder()
                    .id(TEAM_ID).name("Development Team").joinCode(JOIN_CODE).build();

            when(teamRepository.findByJoinCode(JOIN_CODE)).thenReturn(Optional.of(team));
            when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
            when(teamRepository.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0)); // return saved obj
            when(teamApiMapper.toResponse(any(Team.class))).thenReturn(responseDTO);

            TeamResponse result = teamService.joinTeam(request, newUserId);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(TEAM_ID);

            verify(teamRepository).save(teamCaptor.capture());
            assertThat(teamCaptor.getValue().getMemberIds()).contains(newUserId);
        }

        @Test
        @DisplayName("Invalid join code: throws EntityNotFoundException")
        void shouldThrowExceptionWhenJoiningWithInvalidCode() {
            JoinTeamRequest request = new JoinTeamRequest("INVALID", PASSWORD);
            when(teamRepository.findByJoinCode("INVALID")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> teamService.joinTeam(request, USER_ID))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Team not found");

            verify(teamRepository, never()).save(any());
        }

        @Test
        @DisplayName("Wrong password: throws BadCredentialsException")
        void shouldThrowExceptionWhenJoiningWithWrongPassword() {
            JoinTeamRequest request = new JoinTeamRequest(JOIN_CODE, "wrong");

            when(teamRepository.findByJoinCode(JOIN_CODE)).thenReturn(Optional.of(team));
            when(passwordEncoder.matches("wrong", ENCODED_PASSWORD)).thenReturn(false);

            assertThatThrownBy(() -> teamService.joinTeam(request, USER_ID))
                    .isInstanceOf(BadCredentialsException.class);

            verify(teamRepository, never()).save(any());
        }

        @Test
        @DisplayName("User already member: throws IllegalStateException")
        void shouldThrowExceptionWhenUserAlreadyMember() {
            JoinTeamRequest request = new JoinTeamRequest(JOIN_CODE, PASSWORD);

            when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
            when(teamRepository.findByJoinCode(JOIN_CODE)).thenReturn(Optional.of(team));

            assertThatThrownBy(() -> teamService.joinTeam(request, USER_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already a member");

            verify(teamRepository).findByJoinCode(JOIN_CODE);
            verify(teamRepository, never()).save(any());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // getTeamDetails()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getTeamDetails()")
    class GetTeamDetails {

        @Test
        @DisplayName("Team exists: returns details")
        void shouldGetTeamDetails() {
            TeamDetailsProjection projection = mock(TeamDetailsProjection.class);
            TeamDetailsResponse expectedResponse = TeamDetailsResponse.builder().id(TEAM_ID).build();

            when(teamRepository.getTeamDetailsById(TEAM_ID)).thenReturn(Optional.of(projection));
            when(teamApiMapper.toDetailsResponse(projection)).thenReturn(expectedResponse);

            TeamDetailsResponse result = teamService.getTeamDetails(TEAM_ID);

            assertThat(result).isEqualTo(expectedResponse);
        }

        @Test
        @DisplayName("Team missing: throws EntityNotFoundException")
        void shouldThrowExceptionWhenGettingDetailsOfNonExistentTeam() {
            when(teamRepository.getTeamDetailsById(TEAM_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> teamService.getTeamDetails(TEAM_ID))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // kickMember()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("kickMember()")
    class KickMember {

        @Test
        @DisplayName("Owner kicks member: success")
        void shouldKickMemberSuccessfully() {
            UUID memberToKick = UUID.randomUUID();
            team.getMemberIds().add(memberToKick);

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
            when(teamRepository.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));

            teamService.kickMember(TEAM_ID, memberToKick, USER_ID);

            verify(teamRepository).save(teamCaptor.capture());
            assertThat(teamCaptor.getValue().getMemberIds()).doesNotContain(memberToKick);
        }

        @Test
        @DisplayName("Non-owner tries to kick: throws AccessDeniedException")
        void shouldThrowExceptionWhenNonOwnerTriesToKickMember() {
            UUID randomUser = UUID.randomUUID();
            UUID memberToKick = UUID.randomUUID();

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));

            assertThatThrownBy(() -> teamService.kickMember(TEAM_ID, memberToKick, randomUser))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Only owner can kick members");

            verify(teamRepository, never()).save(any());
        }

        @Test
        @DisplayName("Owner tries to kick self: throws IllegalStateException")
        void shouldThrowExceptionWhenTryingToKickOwner() {
            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));

            assertThatThrownBy(() -> teamService.kickMember(TEAM_ID, USER_ID, USER_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot remove the owner from the team");

            verify(teamRepository, never()).save(any());
        }

        @Test
        @DisplayName("Kicking user who is not in team: throws EntityNotFoundException")
        void shouldThrowExceptionWhenKickingNonMember() {
            UUID nonMemberId = UUID.randomUUID();

            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));

            assertThatThrownBy(() -> teamService.kickMember(TEAM_ID, nonMemberId, USER_ID))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("not a member");

            verify(teamRepository, never()).save(any());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // findById()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("Found: returns team")
        void shouldFindTeamById() {
            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));

            Team result = teamService.findById(TEAM_ID);

            assertThat(result).isEqualTo(team);
        }

        @Test
        @DisplayName("Missing: throws EntityNotFoundException")
        void shouldThrowExceptionWhenTeamNotFoundById() {
            when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> teamService.findById(TEAM_ID))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // cleanupDeletedTeams()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("cleanupDeletedTeams()")
    class CleanupDeletedTeams {

        @Test
        @DisplayName("Calculates correct retention date and calls repository")
        void shouldCleanupOldTeams() {
            // Given
            int deletedCount = 5;

            // Захватчик для даты, чтобы проверить логику "минус 30 дней"
            ArgumentCaptor<Instant> dateCaptor = ArgumentCaptor.forClass(Instant.class);

            when(teamRepository.deleteByStatusAndDeletedAtBefore(eq(TeamStatus.DELETED), any(Instant.class)))
                    .thenReturn(deletedCount);

            // When
            teamService.cleanupDeletedTeams();

            // Then
            verify(teamRepository).deleteByStatusAndDeletedAtBefore(eq(TeamStatus.DELETED), dateCaptor.capture());

            Instant capturedDate = dateCaptor.getValue();
            Instant expectedDate = Instant.now().minus(30, java.time.temporal.ChronoUnit.DAYS);

            // Проверяем, что дата совпадает с ожидаемой с точностью до 1 секунды
            // (так как Instant.now() в тесте и в сервисе будут отличаться на миллисекунды)
            assertThat(capturedDate).isCloseTo(expectedDate, org.assertj.core.api.Assertions.within(1, java.time.temporal.ChronoUnit.SECONDS));
        }
    }
}