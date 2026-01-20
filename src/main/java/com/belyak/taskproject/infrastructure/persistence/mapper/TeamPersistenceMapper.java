package com.belyak.taskproject.infrastructure.persistence.mapper;

import com.belyak.taskproject.domain.model.Team;
import com.belyak.taskproject.domain.model.TeamSummary;
import com.belyak.taskproject.infrastructure.persistence.entity.TeamEntity;
import com.belyak.taskproject.infrastructure.persistence.entity.UserEntity;
import com.belyak.taskproject.infrastructure.persistence.projections.TeamSummaryProjection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TeamPersistenceMapper {
    @Mapping(source = "owner", target = "isOwner")
    TeamSummary toSummary(TeamSummaryProjection teamSummaryProjection);

    List<TeamSummary> toSummaryList(List<TeamSummaryProjection> teamSummaryProjections);

    TeamEntity toEntity(Team team);

    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "memberIds", source = "members", qualifiedByName = "mapMembersToIds")
    Team toDomain(TeamEntity entity);

    @Named("mapMembersToIds")
    default Set<UUID> mapMembersToIds(Set<UserEntity> members) {
        if (members == null) return Collections.emptySet();
        return members.stream().map(UserEntity::getId).collect(Collectors.toSet());
    }
}
