package com.belyak.taskproject.infrastructure.persistence.projections;

import java.util.UUID;

public interface TagInfoProjection {
    UUID getId();
    String getName();
    String getColor();
}
