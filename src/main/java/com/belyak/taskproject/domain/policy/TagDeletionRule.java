package com.belyak.taskproject.domain.policy;

import com.belyak.taskproject.domain.model.Tag;

public interface TagDeletionRule {
    void validate(Tag tag);
}
