package com.belyak.taskproject.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
@AllArgsConstructor
public class Tag {
    private final UUID id;
    private String name;
    private String color;
}
