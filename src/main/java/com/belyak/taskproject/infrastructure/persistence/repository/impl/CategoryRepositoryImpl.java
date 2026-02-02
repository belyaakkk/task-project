package com.belyak.taskproject.infrastructure.persistence.repository.impl;

import com.belyak.taskproject.domain.model.Category;
import com.belyak.taskproject.domain.model.TaskStatus;
import com.belyak.taskproject.domain.port.repository.CategoryRepository;
import com.belyak.taskproject.infrastructure.persistence.entity.CategoryEntity;
import com.belyak.taskproject.infrastructure.persistence.entity.TeamEntity;
import com.belyak.taskproject.infrastructure.persistence.mapper.CategoryPersistenceMapper;
import com.belyak.taskproject.infrastructure.persistence.projections.CategoryInfoWithTaskCountProjection;
import com.belyak.taskproject.infrastructure.persistence.repository.SpringDataCategoryRepository;
import com.belyak.taskproject.infrastructure.persistence.repository.SpringDataTeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {

    private final SpringDataCategoryRepository springDataCategoryRepository;
    private final SpringDataTeamRepository springDataTeamRepository;
    private final CategoryPersistenceMapper categoryPersistenceMapper;

    @Override
    public List<CategoryInfoWithTaskCountProjection> findAllByTeamId(UUID teamId, TaskStatus status) {
        return springDataCategoryRepository.findCategoriesByTeamIdAndStatus(teamId, status);
    }

    @Override
    public Category createCategory(UUID teamId, Category category) {
        TeamEntity teamProxy = springDataTeamRepository.getReferenceById(teamId);

        CategoryEntity categoryEntity = CategoryEntity.builder()
                .name(category.getName())
                .team(teamProxy)
                .isSystem(category.isSystem())
                .build();

        CategoryEntity savedEntity = springDataCategoryRepository.save(categoryEntity);

        return categoryPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public boolean existsByName(UUID teamId, String name) {
        return springDataCategoryRepository.existsByTeamIdAndNameIgnoreCase(teamId, name);
    }

    @Override
    public Optional<Category> findById(UUID categoryId) {
        return springDataCategoryRepository.findById(categoryId)
                .map(categoryPersistenceMapper::toDomain);
    }

    @Override
    public void deleteById(UUID categoryId) {
        springDataCategoryRepository.deleteById(categoryId);
    }

    @Override
    public boolean canAccess(UUID categoryId, UUID userId) {
        return springDataCategoryRepository.canAccess(categoryId, userId);
    }
}
