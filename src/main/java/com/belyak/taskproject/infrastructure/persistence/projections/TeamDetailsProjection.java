package com.belyak.taskproject.infrastructure.persistence.projections;

import com.belyak.taskproject.domain.model.TeamStatus;

import java.util.Set;
import java.util.UUID;

public interface TeamDetailsProjection {
    UUID getId();
    String getName();
    TeamStatus getStatus();
    UserInfoProjection getOwner();
    Set<UserInfoProjection> getMembers();
}

