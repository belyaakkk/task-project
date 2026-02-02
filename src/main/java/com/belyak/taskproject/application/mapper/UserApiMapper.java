package com.belyak.taskproject.application.mapper;

import com.belyak.taskproject.domain.model.User;
import com.belyak.taskproject.web.dto.response.UserResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserApiMapper {
    UserResponse toResponse(User user);
}
