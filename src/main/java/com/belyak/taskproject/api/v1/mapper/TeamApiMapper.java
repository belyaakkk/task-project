package com.belyak.taskproject.api.v1.mapper;

import com.belyak.taskproject.api.v1.dto.response.CreateTeamResponse;
import com.belyak.taskproject.api.v1.dto.response.TeamInfoResponse;
import com.belyak.taskproject.api.v1.dto.response.TeamSummaryResponse;
import com.belyak.taskproject.domain.model.Team;
import com.belyak.taskproject.domain.model.TeamSummary;
import com.belyak.taskproject.infrastructure.persistence.projections.TeamDetailsProjection;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface TeamApiMapper {

    TeamSummaryResponse toSummaryResponse(TeamSummary summary);

    List<TeamSummaryResponse> toSummaryResponseList(List<TeamSummary> summaryList);

    CreateTeamResponse toCreateResponse(Team team);

    TeamInfoResponse toInfoResponse(TeamDetailsProjection projection);
}
