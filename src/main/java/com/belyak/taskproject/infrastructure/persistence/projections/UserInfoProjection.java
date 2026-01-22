package com.belyak.taskproject.infrastructure.persistence.projections;

import java.util.UUID;

public interface UserInfoProjection {
    UUID getId();
    String getName();
    String getEmail();
}
