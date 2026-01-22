package com.belyak.taskproject.infrastructure.persistence.projections;

import java.util.UUID;

public interface CategoryInfoProjection {
    UUID getId();
    String getName();
}
