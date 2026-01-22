package com.belyak.taskproject.infrastructure.persistence.projections;

import java.util.UUID;

public interface TagInfoWithTaskCountProjection {
    UUID getId();
    String getName();
    String getColor();
    long getTaskCount();
}
