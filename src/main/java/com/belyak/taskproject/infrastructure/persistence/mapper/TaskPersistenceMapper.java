package com.belyak.taskproject.infrastructure.persistence.mapper;

import com.belyak.taskproject.domain.model.TaskSummary;
import com.belyak.taskproject.infrastructure.persistence.projections.TaskInfoProjection;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaskPersistenceMapper {

    TaskSummary toSummary(TaskInfoProjection taskInfoProjection);
}
