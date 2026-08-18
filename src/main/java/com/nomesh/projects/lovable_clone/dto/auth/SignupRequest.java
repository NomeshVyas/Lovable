package com.nomesh.projects.lovable_clone.dto.auth;

import com.nomesh.projects.lovable_clone.validation.ValidationConstants;
import com.nomesh.projects.lovable_clone.validation.password.ValidPassword;
import com.nomesh.projects.lovable_clone.validation.username.ValidUsername;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank @Email
        String email,

        @ValidUsername
        String username,

        @Size(min = ValidationConstants.NAME_MIN_LENGTH, max = ValidationConstants.NAME_MAX_LENGTH)
        String name,

        @ValidPassword
        String password
) {
}
