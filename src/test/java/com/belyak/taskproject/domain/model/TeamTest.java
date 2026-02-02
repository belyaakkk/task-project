package com.belyak.taskproject.domain.model;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class TeamTest {

    private final UUID ownerId = UUID.randomUUID();

    @Test
    @DisplayName("createNew initializes team with owner in members list and ACTIVE status")
    void createNew_Success() {
        // Given
        String teamName = "Dream Team";
        String joinCode = "CODE123";
        String encodedPassword = "hashedSecret";

        // When
        Team team = Team.createNew(teamName, ownerId, joinCode, encodedPassword);

        // Then
        assertThat(team.getId()).isNull();
        assertThat(team.getName()).isEqualTo(teamName);
        assertThat(team.getJoinCode()).isEqualTo(joinCode);
        assertThat(team.getPassword()).isEqualTo(encodedPassword);
        assertThat(team.getStatus()).isEqualTo(TeamStatus.ACTIVE);

        // Verify owner logic
        assertThat(team.getOwnerId()).isEqualTo(ownerId);
        assertThat(team.getMemberIds()).hasSize(1);
        assertThat(team.getMemberIds()).contains(ownerId);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "  ", "AB"})
    @DisplayName("createNew throws exception when name is invalid (null, empty, or too short)")
    void createNew_InvalidName_ThrowsException(String invalidName) {
        assertThatThrownBy(() -> Team.createNew(invalidName, ownerId, "code", "pass"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Team name must be at least 3 characters");
    }

    @Test
    @DisplayName("addMember adds a new user to the members set")
    void addMember_Success() {
        // Given
        Team team = Team.createNew("Valid Team", ownerId, "code", "pass");
        UUID newMemberId = UUID.randomUUID();

        // When
        team.addMember(newMemberId);

        // Then
        assertThat(team.getMemberIds()).hasSize(2);
        assertThat(team.getMemberIds()).contains(ownerId, newMemberId);
    }

    @Test
    @DisplayName("addMember throws exception when user is already a member")
    void addMember_AlreadyMember_ThrowsException() {
        // Given
        Team team = Team.createNew("Valid Team", ownerId, "code", "pass");

        // When & Then
        assertThatThrownBy(() -> team.addMember(ownerId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("User is already a member of this team");
    }

    @Test
    @DisplayName("removeMember successfully removes a non-owner member")
    void removeMember_Success() {
        // Given
        Team team = Team.createNew("Valid Team", ownerId, "code", "pass");
        UUID memberId = UUID.randomUUID();
        team.addMember(memberId);

        // When
        team.removeMember(memberId);

        // Then
        assertThat(team.getMemberIds()).hasSize(1);
        assertThat(team.getMemberIds()).containsOnly(ownerId); // Only owner remains
    }

    @Test
    @DisplayName("removeMember throws exception when trying to remove the owner")
    void removeMember_Owner_ThrowsException() {
        // Given
        Team team = Team.createNew("Valid Team", ownerId, "code", "pass");

        // When & Then
        assertThatThrownBy(() -> team.removeMember(ownerId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot remove the owner from the team");
    }

    @Test
    @DisplayName("removeMember throws exception when user is not found in team")
    void removeMember_NotFound_ThrowsException() {
        // Given
        Team team = Team.createNew("Valid Team", ownerId, "code", "pass");
        UUID nonMemberId = UUID.randomUUID();

        // When & Then
        assertThatThrownBy(() -> team.removeMember(nonMemberId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User is not a member of this team");
    }

    @Test
    @DisplayName("isPasswordMatch delegates check to PasswordEncoder and returns true on match")
    void isPasswordMatch_True() {
        // Given
        String rawPass = "rawSecret";
        String hashedPass = "hashedSecret";
        Team team = Team.createNew("Team", ownerId, "code", hashedPass);

        // Mocking the encoder
        PasswordEncoder mockEncoder = Mockito.mock(PasswordEncoder.class);
        when(mockEncoder.matches(rawPass, hashedPass)).thenReturn(true);

        // When
        boolean result = team.isPasswordMatch(rawPass, mockEncoder);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isPasswordMatch delegates check to PasswordEncoder and returns false on mismatch")
    void isPasswordMatch_False() {
        // Given
        String rawPass = "wrongPass";
        String hashedPass = "hashedSecret";
        Team team = Team.createNew("Team", ownerId, "code", hashedPass);

        // Mocking the encoder
        PasswordEncoder mockEncoder = Mockito.mock(PasswordEncoder.class);
        when(mockEncoder.matches(anyString(), anyString())).thenReturn(false);

        // When
        boolean result = team.isPasswordMatch(rawPass, mockEncoder);

        // Then
        assertThat(result).isFalse();
    }
}
