package com.nomesh.projects.lovable_clone.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SystemPermission {
    MANAGE_PLANS("plan:manage"),
    VIEW_ALL_USERS("user:view");

    private final String value;
}
