package com.belyak.taskproject.api.v1.mapper;

import com.belyak.taskproject.api.v1.dto.response.CreateTeamResponse;
import com.belyak.taskproject.api.v1.dto.response.TeamSummaryResponse;
import com.belyak.taskproject.domain.model.Team;
import com.belyak.taskproject.domain.model.TeamSummary;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TeamApiMapper {

    TeamSummaryResponse toSummaryResponse(TeamSummary summary);

    List<TeamSummaryResponse> toSummaryResponseList(List<TeamSummary> summaryList);

    CreateTeamResponse toCreateResponse(Team team);
}
