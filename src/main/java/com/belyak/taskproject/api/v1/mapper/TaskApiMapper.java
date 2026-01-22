package com.belyak.taskproject.api.v1.mapper;

import com.belyak.taskproject.api.v1.dto.response.TaskResponse;
import com.belyak.taskproject.domain.model.TaskSummary;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TaskApiMapper {
    TaskResponse toResponse(TaskSummary summary);

    List<TaskResponse> toResponseList(List<TaskSummary> summaryList);
}
