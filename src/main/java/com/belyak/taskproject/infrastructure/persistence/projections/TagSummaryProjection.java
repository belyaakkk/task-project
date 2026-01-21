package com.belyak.taskproject.infrastructure.persistence.projections;

import java.util.UUID;

public interface TagSummaryProjection {
    UUID getId();
    String getName();
    String getColor();
    long getTaskCount();
}
