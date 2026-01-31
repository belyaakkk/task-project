package com.belyak.taskproject.api.v1.mapper;

import com.belyak.taskproject.api.v1.dto.info.UserInfo;
import com.belyak.taskproject.api.v1.dto.response.UserInfoResponse;
import com.belyak.taskproject.domain.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserApiMapper {
    UserInfoResponse toInfoResponse(User user);

    UserInfoResponse toInfoResponse(UserInfo info);
}
