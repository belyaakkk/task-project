package com.belyak.taskproject.infrastructure.persistence.projections;

import java.util.UUID;

public interface CategorySummaryProjection {
    UUID getId();
    String getName();
    long getTaskCount();
}
