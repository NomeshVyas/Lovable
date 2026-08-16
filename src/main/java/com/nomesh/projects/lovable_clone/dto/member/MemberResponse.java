package com.nomesh.projects.lovable_clone.dto.member;

import com.nomesh.projects.lovable_clone.entity.ProjectRole;

import java.time.Instant;

public record MemberResponse(
        Long userId,
        String name,
        String username,
        String email,
        ProjectRole projectRole,
        Instant invitedAt
) {
}
