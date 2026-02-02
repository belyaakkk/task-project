package com.belyak.taskproject.infrastructure.security.access;

import com.belyak.taskproject.domain.port.repository.TagRepository;
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
