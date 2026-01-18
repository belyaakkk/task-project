package com.belyak.taskproject.infrastructure.persistence.repository;

import com.belyak.taskproject.AbstractIntegrationTest;
import com.belyak.taskproject.config.JpaConfig;
import com.belyak.taskproject.domain.model.TaskStatus;
import com.belyak.taskproject.infrastructure.persistence.entity.CategoryEntity;
import com.belyak.taskproject.infrastructure.persistence.entity.TaskEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaConfig.class)
class SpringDataCategoryRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private SpringDataCategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findCategoriesWithTaskCount_shouldCountOnlyPublishedTasks() {
        CategoryEntity category = CategoryEntity.builder().name("Java").build();
        entityManager.persist(category);

        TaskEntity task1 = TaskEntity.builder().title("Task 1").status(TaskStatus.PUBLISHED).category(category).content("Test Content 1").build();
        TaskEntity task2 = TaskEntity.builder().title("Task 2").status(TaskStatus.DRAFT).category(category).content("Test content 2").build();
        entityManager.persist(task1);
        entityManager.persist(task2);

        entityManager.flush();

        var result = categoryRepository.findCategoriesWithTaskCount(TaskStatus.PUBLISHED);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Java");
        assertThat(result.getFirst().getTaskCount()).isEqualTo(1);
    }
}