package com.nomesh.projects.lovable_clone.entity;

import lombok.Getter;

import java.util.Set;

import static com.nomesh.projects.lovable_clone.entity.SystemPermission.MANAGE_PLANS;
import static com.nomesh.projects.lovable_clone.entity.SystemPermission.VIEW_ALL_USERS;

@Getter
public enum SystemRole {
    USER(),
    ADMIN(MANAGE_PLANS, VIEW_ALL_USERS);

    private final Set<SystemPermission> permissions;

    SystemRole(SystemPermission... permissions) {
        this.permissions = Set.of(permissions);
    }
}
