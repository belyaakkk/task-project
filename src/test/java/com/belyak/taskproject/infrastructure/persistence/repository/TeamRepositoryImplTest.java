package com.belyak.taskproject.infrastructure.persistence.repository;

import com.belyak.taskproject.domain.model.Team;
import com.belyak.taskproject.domain.model.TeamStatus;
import com.belyak.taskproject.infrastructure.persistence.entity.TeamEntity;
import com.belyak.taskproject.infrastructure.persistence.entity.UserEntity;
import com.belyak.taskproject.infrastructure.persistence.mapper.TeamPersistenceMapper;
import com.belyak.taskproject.infrastructure.persistence.projections.TeamDetailsProjection;
import com.belyak.taskproject.infrastructure.persistence.projections.TeamSummaryProjection;
import com.belyak.taskproject.infrastructure.persistence.repository.impl.TeamRepositoryImpl;
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
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamRepositoryImplTest {

    // ── SUT ─────────────────────────────────────────────────────────────────
    private TeamRepositoryImpl repositoryImpl;

    // ── Collaborators ────────────────────────────────────────────────────────
    @Mock private SpringDataTeamRepository springDataTeamRepository;
    @Mock
    private SpringDataUserRepository springDataUserRepository;
    @Mock private TeamPersistenceMapper teamPersistenceMapper;

    // ── Fixtures ─────────────────────────────────────────────────────────────
    private static final UUID   TEAM_ID   = UUID.randomUUID();
    private static final UUID   OWNER_ID  = UUID.randomUUID();
    private static final UUID   USER_ID_1 = UUID.randomUUID();
    private static final UUID   USER_ID_2 = UUID.randomUUID();
    private static final String JOIN_CODE = "ABC-123";

    private TeamEntity teamEntity;
    private UserEntity ownerProxy, memberProxy1, memberProxy2;
    private Team domainTeam;

    @BeforeEach
    void setUp() {
        repositoryImpl = new TeamRepositoryImpl(
                springDataTeamRepository,
                springDataUserRepository,
                teamPersistenceMapper
        );

        ownerProxy   = new UserEntity();
        memberProxy1 = new UserEntity();
        memberProxy2 = new UserEntity();

        teamEntity = new TeamEntity();
        teamEntity.setMembers(new HashSet<>());   // mutable

        domainTeam = Team.builder()
                .id(null)
                .name("Dev Team")
                .joinCode(JOIN_CODE)
                .password("secret")
                .status(TeamStatus.ACTIVE)
                .ownerId(OWNER_ID)
                .memberIds(Set.of(USER_ID_1, USER_ID_2))
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // getAllByMemberId()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getAllByMemberId()")
    class GetAllByMemberId {

        @Test
        @DisplayName("Returns projections for user's teams")
        void returnsProjections() {
            TeamSummaryProjection p1 = mock(TeamSummaryProjection.class);
            TeamSummaryProjection p2 = mock(TeamSummaryProjection.class);
            List<TeamSummaryProjection> expected = List.of(p1, p2);

            when(springDataTeamRepository.findTeamsSummaryByMemberIdAndStatus(OWNER_ID, TeamStatus.ACTIVE))
                    .thenReturn(expected);

            List<TeamSummaryProjection> result = repositoryImpl.getAllByMemberId(OWNER_ID);

            assertThat(result).hasSize(2).containsExactly(p1, p2);
            verify(springDataTeamRepository).findTeamsSummaryByMemberIdAndStatus(OWNER_ID, TeamStatus.ACTIVE);
        }

        @Test
        @DisplayName("User not in any team: returns empty list")
        void whenNoTeams_returnsEmptyList() {
            when(springDataTeamRepository.findTeamsSummaryByMemberIdAndStatus(OWNER_ID, TeamStatus.ACTIVE))
                    .thenReturn(Collections.emptyList());

            assertThat(repositoryImpl.getAllByMemberId(OWNER_ID)).isEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // save()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("save()")
    class Save {

        // ── NEW entity ────────────────────────────────────────────────────────
        @Test
        @DisplayName("New team (id == null): creates entity, sets owner + members")
        void whenIdIsNull_createsNewEntity() {
            when(springDataUserRepository.getReferenceById(OWNER_ID))  .thenReturn(ownerProxy);
            when(springDataUserRepository.getReferenceById(USER_ID_1)).thenReturn(memberProxy1);
            when(springDataUserRepository.getReferenceById(USER_ID_2)).thenReturn(memberProxy2);
            when(springDataTeamRepository.save(any(TeamEntity.class))).thenReturn(teamEntity);
            when(teamPersistenceMapper.toDomain(teamEntity))          .thenReturn(domainTeam);

            Team result = repositoryImpl.save(domainTeam);

            assertThat(result).isEqualTo(domainTeam);

            ArgumentCaptor<TeamEntity> captor = ArgumentCaptor.forClass(TeamEntity.class);
            verify(springDataTeamRepository).save(captor.capture());
            TeamEntity persisted = captor.getValue();

            assertThat(persisted.getName())     .isEqualTo("Dev Team");
            assertThat(persisted.getJoinCode()) .isEqualTo(JOIN_CODE);
            assertThat(persisted.getPassword()) .isEqualTo("secret");
            assertThat(persisted.getStatus())   .isEqualTo(TeamStatus.ACTIVE);
            assertThat(persisted.getOwner())    .isEqualTo(ownerProxy);

            assertThat(persisted.getMembers())  .contains(ownerProxy, memberProxy1, memberProxy2);
        }

        // ── EXISTING entity (found) ───────────────────────────────────────────
        @Test
        @DisplayName("Existing team (found): updates fields")
        void whenIdIsNotNull_andFound_updatesEntity() {
            Team updateTeam = Team.builder()
                    .id(TEAM_ID)
                    .name("Renamed Team")
                    .joinCode("NEW-CODE")
                    .password("newPass")
                    .status(TeamStatus.ACTIVE)
                    .ownerId(OWNER_ID)
                    .memberIds(Set.of(USER_ID_1))
                    .build();

            TeamEntity existingEntity = new TeamEntity();
            existingEntity.setMembers(new HashSet<>());

            when(springDataTeamRepository.findById(TEAM_ID))        .thenReturn(Optional.of(existingEntity));
            when(springDataUserRepository.getReferenceById(OWNER_ID))  .thenReturn(ownerProxy);
            when(springDataUserRepository.getReferenceById(USER_ID_1)).thenReturn(memberProxy1);
            when(springDataTeamRepository.save(existingEntity))     .thenReturn(existingEntity);
            when(teamPersistenceMapper.toDomain(existingEntity))    .thenReturn(updateTeam);

            Team result = repositoryImpl.save(updateTeam);

            assertThat(result).isEqualTo(updateTeam);
            assertThat(existingEntity.getName())     .isEqualTo("Renamed Team");
            assertThat(existingEntity.getJoinCode()) .isEqualTo("NEW-CODE");
            assertThat(existingEntity.getPassword()) .isEqualTo("newPass");
        }

        // ── EXISTING entity (NOT found) ──────────────────────────────────────
        @Test
        @DisplayName("Existing team (missing): throws EntityNotFoundException")
        void whenIdIsNotNull_andMissing_throwsEntityNotFoundException() {
            Team ghost = Team.builder()
                    .id(TEAM_ID)
                    .name("Ghost")
                    .joinCode("")
                    .password("")
                    .status(TeamStatus.ACTIVE)
                    .ownerId(OWNER_ID)
                    .memberIds(Set.of())
                    .build();

            when(springDataTeamRepository.findById(TEAM_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> repositoryImpl.save(ghost))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(TEAM_ID.toString());
        }

        // ── owner added to members ──────────────────────────────────────        @Test
        @DisplayName("Owner is always added to members list (even if not in memberIds)")
        void ownerIsAlwaysInMembers() {

            Team noMembers = Team.builder()
                    .id(null)
                    .name("Solo")
                    .joinCode("SOLO")
                    .password("p")
                    .status(TeamStatus.ACTIVE)
                    .ownerId(OWNER_ID)
                    .memberIds(Set.of())
                    .build();

            when(springDataUserRepository.getReferenceById(OWNER_ID)).thenReturn(ownerProxy);
            when(springDataTeamRepository.save(any(TeamEntity.class))).thenReturn(teamEntity);
            when(teamPersistenceMapper.toDomain(teamEntity))         .thenReturn(noMembers);

            repositoryImpl.save(noMembers);

            ArgumentCaptor<TeamEntity> captor = ArgumentCaptor.forClass(TeamEntity.class);
            verify(springDataTeamRepository).save(captor.capture());

            assertThat(captor.getValue().getMembers()).containsExactly(ownerProxy);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // existsByJoinCode()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("existsByJoinCode()")
    class ExistsByJoinCode {

        @Test
        @DisplayName("Code exists -> true")
        void whenExists_returnsTrue() {
            when(springDataTeamRepository.existsByJoinCode(JOIN_CODE)).thenReturn(true);
            assertThat(repositoryImpl.existsByJoinCode(JOIN_CODE)).isTrue();
        }

        @Test
        @DisplayName("Code missing -> false")
        void whenMissing_returnsFalse() {
            when(springDataTeamRepository.existsByJoinCode(JOIN_CODE)).thenReturn(false);
            assertThat(repositoryImpl.existsByJoinCode(JOIN_CODE)).isFalse();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // findByJoinCode()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findByJoinCode()")
    class FindByJoinCode {

        @Test
        @DisplayName("Found: Optional with domain model")
        void whenFound_returnsOptional() {
            when(springDataTeamRepository.findByJoinCode(JOIN_CODE)).thenReturn(Optional.of(teamEntity));
            when(teamPersistenceMapper.toDomain(teamEntity))       .thenReturn(domainTeam);

            assertThat(repositoryImpl.findByJoinCode(JOIN_CODE)).isPresent().contains(domainTeam);
        }

        @Test
        @DisplayName("Missing: Optional.empty()")
        void whenMissing_returnsEmpty() {
            when(springDataTeamRepository.findByJoinCode(JOIN_CODE)).thenReturn(Optional.empty());

            assertThat(repositoryImpl.findByJoinCode(JOIN_CODE)).isEmpty();
            verify(teamPersistenceMapper, never()).toDomain(any());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // isMember() / isOwner()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("isMember() / isOwner()")
    class MembershipChecks {

        @Test
        @DisplayName("isMember - user is a member")
        void isMember_true() {
            when(springDataTeamRepository.isMember(TEAM_ID, USER_ID_1)).thenReturn(true);
            assertThat(repositoryImpl.isMember(TEAM_ID, USER_ID_1)).isTrue();
        }

        @Test
        @DisplayName("isMember - user is NOT a member")
        void isMember_false() {
            when(springDataTeamRepository.isMember(TEAM_ID, USER_ID_1)).thenReturn(false);
            assertThat(repositoryImpl.isMember(TEAM_ID, USER_ID_1)).isFalse();
        }

        @Test
        @DisplayName("isOwner - user is the owner")
        void isOwner_true() {
            when(springDataTeamRepository.isOwner(TEAM_ID, OWNER_ID)).thenReturn(true);
            assertThat(repositoryImpl.isOwner(TEAM_ID, OWNER_ID)).isTrue();
        }

        @Test
        @DisplayName("isOwner - user is NOT the owner")
        void isOwner_false() {
            when(springDataTeamRepository.isOwner(TEAM_ID, USER_ID_1)).thenReturn(false);
            assertThat(repositoryImpl.isOwner(TEAM_ID, USER_ID_1)).isFalse();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // findById() / existsById()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("findById() / existsById()")
    class FindAndExists {

        @Test
        @DisplayName("findById - found")
        void findById_present() {
            when(springDataTeamRepository.findById(TEAM_ID)).thenReturn(Optional.of(teamEntity));
            when(teamPersistenceMapper.toDomain(teamEntity)).thenReturn(domainTeam);

            assertThat(repositoryImpl.findById(TEAM_ID)).isPresent().contains(domainTeam);
        }

        @Test
        @DisplayName("findById - not found")
        void findById_empty() {
            when(springDataTeamRepository.findById(TEAM_ID)).thenReturn(Optional.empty());

            assertThat(repositoryImpl.findById(TEAM_ID)).isEmpty();
        }

        @Test
        @DisplayName("existsById — true")
        void existsById_true() {
            when(springDataTeamRepository.existsById(TEAM_ID)).thenReturn(true);
            assertThat(repositoryImpl.existsById(TEAM_ID)).isTrue();
        }

        @Test
        @DisplayName("existsById — false")
        void existsById_false() {
            when(springDataTeamRepository.existsById(TEAM_ID)).thenReturn(false);
            assertThat(repositoryImpl.existsById(TEAM_ID)).isFalse();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // deleteByStatusAndDeletedAtBefore()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("deleteByStatusAndDeletedAtBefore()")
    class SoftDeleteCleanup {

        @Test
        @DisplayName("N teams deleted - returns count")
        void returnsNumberOfDeletedTeams() {
            Instant cutoff = Instant.now().minusSeconds(86400);
            when(springDataTeamRepository.deleteByStatusAndDeletedAtBefore(TeamStatus.ACTIVE, cutoff))
                    .thenReturn(3);

            int count = repositoryImpl.deleteByStatusAndDeletedAtBefore(TeamStatus.ACTIVE, cutoff);

            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("Nothing deleted - returns 0")
        void returnsZeroWhenNothingDeleted() {
            Instant cutoff = Instant.now();
            when(springDataTeamRepository.deleteByStatusAndDeletedAtBefore(TeamStatus.ACTIVE, cutoff))
                    .thenReturn(0);

            assertThat(repositoryImpl.deleteByStatusAndDeletedAtBefore(TeamStatus.ACTIVE, cutoff)).isZero();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // getTeamDetailsById()
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getTeamDetailsById()")
    class GetTeamDetailsById {

        @Test
        @DisplayName("Details found: Optional with projection")
        void whenFound_returnsOptionalProjection() {
            TeamDetailsProjection proj = mock(TeamDetailsProjection.class);
            when(springDataTeamRepository.findProjectedById(TEAM_ID)).thenReturn(Optional.of(proj));

            assertThat(repositoryImpl.getTeamDetailsById(TEAM_ID)).isPresent().contains(proj);
        }

        @Test
        @DisplayName("Missing: Optional.empty()")
        void whenMissing_returnsEmptyOptional() {
            when(springDataTeamRepository.findProjectedById(TEAM_ID)).thenReturn(Optional.empty());

            assertThat(repositoryImpl.getTeamDetailsById(TEAM_ID)).isEmpty();
        }
    }
}
