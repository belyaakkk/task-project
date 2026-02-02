package com.belyak.taskproject.infrastructure.persistence.mapper;

import com.belyak.taskproject.domain.model.Team;
import com.belyak.taskproject.infrastructure.persistence.entity.TeamEntity;
import com.belyak.taskproject.infrastructure.persistence.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TeamPersistenceMapper {

    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "memberIds", source = "members", qualifiedByName = "mapMembersToIds")
    Team toDomain(TeamEntity entity);

    @Named("mapMembersToIds")
    default Set<UUID> mapMembersToIds(Set<UserEntity> members) {
        if (members == null || members.isEmpty()) {
            return Collections.emptySet();
        }
        return members.stream()
                .map(UserEntity::getId)
                .collect(Collectors.toSet());
    }
}
