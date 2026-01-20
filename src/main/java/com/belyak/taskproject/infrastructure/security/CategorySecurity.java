package com.belyak.taskproject.infrastructure.security;

import com.belyak.taskproject.domain.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("categorySecurity")
@RequiredArgsConstructor
public class CategorySecurity {

    private final CategoryRepository categoryRepository;

    public boolean hasAccess(UUID categoryId, UUID userId) {
        return categoryRepository.canAccess(categoryId, userId);
    }
}
