package com.nomesh.projects.lovable_clone.dto.auth;

import com.nomesh.projects.lovable_clone.validation.password.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank @Email
        String email,

        @Size(min = 1, max = 50)
        String name,

        @ValidPassword
        String password
) {
}
