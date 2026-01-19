package com.belyak.taskproject.infrastructure.persistence.projections;

import java.util.UUID;

public interface TeamSummaryProjection {
    UUID getId();
    String getName();
    boolean isOwner();
    int getMemberCount();
}
