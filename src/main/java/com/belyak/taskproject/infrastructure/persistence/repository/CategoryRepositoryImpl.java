package com.belyak.taskproject.infrastructure.persistence.repository;

import com.belyak.taskproject.domain.model.Category;
import com.belyak.taskproject.domain.model.CategorySummary;
import com.belyak.taskproject.domain.model.TaskStatus;
import com.belyak.taskproject.domain.repository.CategoryRepository;
import com.belyak.taskproject.infrastructure.persistence.entity.CategoryEntity;
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
    private final CategoryPersistenceMapper categoryPersistenceMapper;

    @Override
    public List<CategorySummary> findAllSummaries(TaskStatus status) {
        return springDataCategoryRepository.findCategoriesWithTaskCount(status).stream()
                .map(categoryPersistenceMapper::toSummary)
                .toList();
    }

    @Override
    public Category save(Category category) {
        CategoryEntity categoryToSave = categoryPersistenceMapper.toEntity(category);
        CategoryEntity savedCategory = springDataCategoryRepository.save(categoryToSave);
        return categoryPersistenceMapper.toDomain(savedCategory);
    }

    @Override
    public boolean existsByNameIgnoreCase(String name) {
        return springDataCategoryRepository.existsByNameIgnoreCase(name);
    }

    @Override
    public Optional<Category> findById(UUID categoryId) {
        return springDataCategoryRepository.findById(categoryId).map(categoryPersistenceMapper::toDomain);
    }

    @Override
    public Optional<CategorySummary> findSummaryById(UUID categoryId, TaskStatus status) {
        return springDataCategoryRepository.findCategoryWithTaskCountById(categoryId, status)
                .map(categoryPersistenceMapper::toSummary);
    }

    @Override
    public void deleteById(UUID categoryId) {
        springDataCategoryRepository.deleteById(categoryId);
    }

    @Override
    public boolean existsById(UUID categoryId) {
        return springDataCategoryRepository.existsById(categoryId);
    }
}
