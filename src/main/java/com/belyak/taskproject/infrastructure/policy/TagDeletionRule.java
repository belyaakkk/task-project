package com.belyak.taskproject.infrastructure.policy;

import com.belyak.taskproject.domain.model.Tag;

public interface TagDeletionRule {
    void validate(Tag tag);
}
