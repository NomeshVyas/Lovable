package com.nomesh.projects.lovable_clone.entity;

import lombok.Getter;

import java.util.Set;
import static com.nomesh.projects.lovable_clone.entity.ProjectPermission.*;

@Getter
public enum ProjectRole {
    OWNER(VIEW, EDIT, DELETE, VIEW_MEMBERS, MANAGE_MEMBERS),
    EDITOR(VIEW, EDIT, DELETE, VIEW_MEMBERS),
    VIEWER(VIEW, VIEW_MEMBERS);

    private final Set<ProjectPermission> permissions;

    ProjectRole(ProjectPermission... permissions) {
        this.permissions = Set.of(permissions);
    }
}
