package com.nomesh.projects.lovable_clone.mapper;

import com.nomesh.projects.lovable_clone.dto.member.MemberResponse;
import com.nomesh.projects.lovable_clone.entity.ProjectMember;
import com.nomesh.projects.lovable_clone.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMemberMapper {

    @Mapping(target = "userId", source = "id" )
    @Mapping(target = "projectRole", constant = "OWNER")
    MemberResponse toMemberResponse(User owner);

    @Mapping(target = "projectRole", source = "projectRole")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "name", source = "user.name")
    MemberResponse toMemberResponse(ProjectMember projectMember);
}
