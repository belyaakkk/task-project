package com.belyak.taskproject.infrastructure.security;

import com.belyak.taskproject.domain.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("tagSecurity")
@RequiredArgsConstructor
public class TagSecurity {

    private final TagRepository tagRepository;

    public boolean hasAccess(UUID tagId, UUID userId) {
        return tagRepository.canAccess(tagId, userId);
    }
}
