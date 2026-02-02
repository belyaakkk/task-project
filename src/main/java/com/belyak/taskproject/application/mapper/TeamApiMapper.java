package com.belyak.taskproject.application.mapper;

import com.belyak.taskproject.domain.model.Team;
import com.belyak.taskproject.infrastructure.persistence.projections.TeamDetailsProjection;
import com.belyak.taskproject.infrastructure.persistence.projections.TeamSummaryProjection;
import com.belyak.taskproject.web.dto.response.TeamDetailsResponse;
import com.belyak.taskproject.web.dto.response.TeamResponse;
import com.belyak.taskproject.web.dto.response.TeamSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface TeamApiMapper {

    @Mapping(target = "isOwner", source = "owner")
    TeamSummaryResponse toSummaryResponse(TeamSummaryProjection projection);

    List<TeamSummaryResponse> toSummaryResponseList(List<TeamSummaryProjection> projections);

    TeamResponse toResponse(Team team);

    TeamDetailsResponse toDetailsResponse(TeamDetailsProjection projection);
}
