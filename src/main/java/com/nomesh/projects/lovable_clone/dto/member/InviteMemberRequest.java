package com.nomesh.projects.lovable_clone.dto.member;

import com.nomesh.projects.lovable_clone.entity.ProjectRole;
import com.nomesh.projects.lovable_clone.validation.login.AtLeastOneIdentifier;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

@AtLeastOneIdentifier
public record InviteMemberRequest(
        @Email
        String email,

        String username,

        @NotNull
        ProjectRole role
) {
}
