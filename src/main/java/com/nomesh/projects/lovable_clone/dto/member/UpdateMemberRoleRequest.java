package com.nomesh.projects.lovable_clone.dto.member;

import com.nomesh.projects.lovable_clone.entity.ProjectRole;
import jakarta.validation.constraints.NotNull;

public record UpdateMemberRoleRequest(
        @NotNull
        ProjectRole role
) {
}
