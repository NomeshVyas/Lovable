package com.nomesh.projects.lovable_clone.mapper;

import com.nomesh.projects.lovable_clone.dto.auth.AuthResponse;
import com.nomesh.projects.lovable_clone.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "userProfileResponse", source = "user")
    AuthResponse toAuthResponse(String token, User user);
}
