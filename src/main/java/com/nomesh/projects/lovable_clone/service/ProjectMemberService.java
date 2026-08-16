package com.nomesh.projects.lovable_clone.service;

import com.nomesh.projects.lovable_clone.dto.member.InviteMemberRequest;
import com.nomesh.projects.lovable_clone.dto.member.MemberResponse;
import com.nomesh.projects.lovable_clone.dto.member.UpdateMemberRoleRequest;
import com.nomesh.projects.lovable_clone.entity.Project;
import com.nomesh.projects.lovable_clone.entity.ProjectMember;
import com.nomesh.projects.lovable_clone.entity.ProjectRole;
import com.nomesh.projects.lovable_clone.entity.User;

import java.util.List;

public interface ProjectMemberService {
    List<MemberResponse> getProjectMembers(Long projectId, Long userId);

    MemberResponse inviteMember(Long projectId, InviteMemberRequest request, Long userId);

    MemberResponse updateMemberRole(Long projectId, Long memberId, UpdateMemberRoleRequest request, Long userId);

    void removeProjectMember(Long projectId, Long memberId, Long userId);

    ProjectMember addProjectMember(Project project, User user, ProjectRole projectRole);
}
