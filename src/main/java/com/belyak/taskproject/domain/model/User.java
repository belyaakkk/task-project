package com.belyak.taskproject.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class User {
    private final UUID id;
    private String email;
    private final String password;
    private String name;
    private Role role;
}
