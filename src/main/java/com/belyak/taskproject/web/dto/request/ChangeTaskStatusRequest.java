package com.belyak.taskproject.web.dto.request;

import com.belyak.taskproject.domain.model.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
@Schema(description = "Request to change task status")
public record ChangeTaskStatusRequest(

        @Schema(description = "New task status", example = "IN_PROGRESS")
        @NotNull(message = "Status is required")
        TaskStatus status
) {
}