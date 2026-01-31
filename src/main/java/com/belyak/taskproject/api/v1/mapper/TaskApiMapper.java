package com.belyak.taskproject.api.v1.mapper;

import com.belyak.taskproject.api.v1.dto.response.CreateTaskResponse;
import com.belyak.taskproject.api.v1.dto.response.TaskResponse;
import com.belyak.taskproject.domain.model.Task;
import com.belyak.taskproject.domain.model.TaskSummary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TaskApiMapper {
    TaskResponse toResponse(TaskSummary summary);

    List<TaskResponse> toResponseList(List<TaskSummary> summaryList);

    @Mapping(target = "assigneeId", source = "assignee")
    @Mapping(target = "categoryId", source = "categoryId")
    @Mapping(target = "createdAt", source = "createdAt")
    CreateTaskResponse toCreateResponse(Task task);
}
