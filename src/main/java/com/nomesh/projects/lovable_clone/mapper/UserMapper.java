package com.nomesh.projects.lovable_clone.mapper;

import com.nomesh.projects.lovable_clone.dto.auth.SignupRequest;
import com.nomesh.projects.lovable_clone.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(SignupRequest signupRequest);
}
