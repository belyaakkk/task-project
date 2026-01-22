package com.belyak.taskproject.infrastructure.persistence.projections;

import java.util.UUID;

public interface CategoryInfoWithTaskCountProjection {
    UUID getId();
    String getName();
    long getTaskCount();
}
