package com.nomesh.projects.lovable_clone.dto.auth;

import com.nomesh.projects.lovable_clone.validation.login.AtLeastOneIdentifier;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@AtLeastOneIdentifier
public record LoginRequest(
    @Email
    String email,

    String username,

    @NotBlank
    String password
) {
}
