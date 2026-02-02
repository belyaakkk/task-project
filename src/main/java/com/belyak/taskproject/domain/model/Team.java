package com.belyak.taskproject.domain.model;

import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Team {

    private final UUID id;
    private final String name;
    private final String joinCode;
    private final String password;
    private final TeamStatus status;
    private final UUID ownerId;
    private final Set<UUID> memberIds;

    public static Team createNew(String name, UUID ownerId, String joinCode, String encodedPassword) {
        validateName(name);

        Set<UUID> initialMembers = new HashSet<>();
        initialMembers.add(ownerId);

        return new Team(
                null,
                name,
                joinCode,
                encodedPassword,
                TeamStatus.ACTIVE,
                ownerId,
                initialMembers
        );
    }

    public void addMember(UUID newMemberId) {
        if (memberIds.contains(newMemberId)) {
            throw new IllegalStateException("User is already a member of this team");
        }
        this.memberIds.add(newMemberId);
    }

    public void removeMember(UUID memberId) {
        if (memberId.equals(ownerId)) {
            throw new IllegalStateException("Cannot remove the owner from the team");
        }
        if (!memberIds.contains(memberId)) {
            throw new EntityNotFoundException("User is not a member of this team");
        }
        this.memberIds.remove(memberId);
    }

    public boolean isPasswordMatch(String rawPassword, PasswordEncoder encoder) {
        return encoder.matches(rawPassword, this.password);
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank() || name.length() < 3) {
            throw new IllegalArgumentException("Team name must be at least 3 characters");
        }
    }
}
