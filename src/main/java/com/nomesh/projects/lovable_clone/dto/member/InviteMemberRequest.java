package com.nomesh.projects.lovable_clone.dto.member;

import com.nomesh.projects.lovable_clone.entity.ProjectRole;

public record InviteMemberRequest(
        String email,
        ProjectRole role
) {
}
