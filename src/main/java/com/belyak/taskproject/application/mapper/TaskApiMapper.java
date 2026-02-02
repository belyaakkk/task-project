package com.belyak.taskproject.application.mapper;

import com.belyak.taskproject.domain.model.Task;
import com.belyak.taskproject.infrastructure.persistence.projections.TaskInfoProjection;
import com.belyak.taskproject.web.dto.response.CreateTaskResponse;
import com.belyak.taskproject.web.dto.response.TaskResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CategoryApiMapper.class, TagApiMapper.class, UserApiMapper.class})
public interface TaskApiMapper {

    TaskResponse toResponse(TaskInfoProjection projection);

    List<TaskResponse> toResponseList(List<TaskInfoProjection> projections);

    @Mapping(target = "assigneeId", source = "assigneeId")
    @Mapping(target = "categoryId", source = "categoryId")
    CreateTaskResponse toCreateResponse(Task task);
}
