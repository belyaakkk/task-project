package com.belyak.taskproject.infrastructure.persistence.entity;

import com.belyak.taskproject.domain.model.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Builder.Default
    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    private List<TeamEntity> ownedTeams = new ArrayList<>();

    @Builder.Default
    @ManyToMany(mappedBy = "members", fetch = FetchType.LAZY)
    private Set<TeamEntity> joinedTeams = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "assignee", fetch = FetchType.LAZY)
    private List<TaskEntity> tasks = new ArrayList<>();
}
