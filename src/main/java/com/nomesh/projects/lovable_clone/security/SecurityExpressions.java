package com.nomesh.projects.lovable_clone.security;

import com.nomesh.projects.lovable_clone.entity.ProjectPermission;
import com.nomesh.projects.lovable_clone.entity.ProjectRole;
import com.nomesh.projects.lovable_clone.repository.ProjectMemberRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component("security")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class SecurityExpressions {

    ProjectMemberRepository projectMemberRepository;
    AuthUtil authUtil;

    public boolean canViewProject(Long projectId) {
        return hasPermission(projectId, ProjectPermission.VIEW);
    }

    public boolean canEditProject(Long projectId) {
        return hasPermission(projectId, ProjectPermission.EDIT);
    }

    public boolean canDeleteProject(Long projectId) {
        return hasPermission(projectId, ProjectPermission.DELETE);
    }

    public boolean canViewProjectMembers(Long projectId) {
        return hasPermission(projectId, ProjectPermission.VIEW_MEMBERS);
    }

    public boolean canManageProjectMembers(Long projectId) {
        return hasPermission(projectId, ProjectPermission.MANAGE_MEMBERS);
    }

    private boolean hasPermission(Long projectId, ProjectPermission permission) {
        Long userId = authUtil.getCurrentUserId();
        return projectMemberRepository.findProjectRoleByProjectIdAndUserId(projectId, userId)
                .map(role -> role.getPermissions().contains(permission))
                .orElse(false);
    }
}