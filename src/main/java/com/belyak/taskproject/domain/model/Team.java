package com.belyak.taskproject.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
public class Team {
    private UUID id;
    private String name;
    private String joinCode;
    private String password;
    private TeamStatus status;
    private UUID owner;
    private Set<UUID> members;
}
