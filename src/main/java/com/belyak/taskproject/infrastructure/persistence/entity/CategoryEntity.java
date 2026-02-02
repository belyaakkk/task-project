package com.belyak.taskproject.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "categories", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"team_id", "name"})
})
public class CategoryEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Builder.Default
    @Column(name = "is_system", nullable = false)
    private boolean isSystem = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private TeamEntity team;

    @Builder.Default
    @OneToMany(mappedBy = "category")
    private Set<TaskEntity> tasks = new HashSet<>();
}


