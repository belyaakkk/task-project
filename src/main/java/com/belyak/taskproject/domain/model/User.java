package com.belyak.taskproject.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
@AllArgsConstructor
public class User {
    private final UUID id;
    private final String email;
    private final String password;
    private final String name;
    private Role role;
}
