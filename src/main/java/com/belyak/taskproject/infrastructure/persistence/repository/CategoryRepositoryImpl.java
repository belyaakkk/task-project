package com.belyak.taskproject.infrastructure.persistence.repository;

import com.belyak.taskproject.domain.model.Category;
import com.belyak.taskproject.domain.model.CategorySummary;
import com.belyak.taskproject.domain.model.TaskStatus;
import com.belyak.taskproject.domain.repository.CategoryRepository;
import com.belyak.taskproject.infrastructure.persistence.entity.CategoryEntity;
import com.belyak.taskproject.infrastructure.persistence.entity.TeamEntity;
import com.belyak.taskproject.infrastructure.persistence.mapper.CategoryPersistenceMapper;
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
    public List<CategorySummary> findAllByTeamId(UUID teamId, TaskStatus status) {
        return springDataCategoryRepository.findCategoriesByTeamIdAndStatus(teamId, status).stream()
                .map(categoryPersistenceMapper::toSummary)
                .toList();
    }

    @Override
    public Category createCategory(UUID teamId, String name) {
        TeamEntity teamProxy = springDataTeamRepository.getReferenceById(teamId);

        CategoryEntity categoryEntity = CategoryEntity.builder()
                .name(name)
                .team(teamProxy)
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
        return springDataCategoryRepository.findById(categoryId).map(categoryPersistenceMapper::toDomain);
    }

    @Override
    public void deleteById(UUID categoryId) {
        springDataCategoryRepository.deleteById(categoryId);
    }

    @Override
    public boolean existsById(UUID categoryId) {
        return springDataCategoryRepository.existsById(categoryId);
    }

    @Override
    public boolean canAccess(UUID categoryId, UUID userId) {
        return springDataCategoryRepository.canAccess(categoryId, userId);
    }
}
