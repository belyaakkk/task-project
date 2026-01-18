package com.belyak.taskproject.domain.event;

import java.util.UUID;

public record CategoryDeleteEvent(UUID categoryId) {
}
