package com.nomesh.projects.lovable_clone.validation.login;

import com.nomesh.projects.lovable_clone.dto.auth.LoginRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AtLeastOneIdentifierValidator implements ConstraintValidator<AtLeastOneIdentifier, LoginRequest> {

    @Override
    public boolean isValid(LoginRequest loginRequest, ConstraintValidatorContext context) {
        if (loginRequest == null) return true;
        return (loginRequest.email() != null && !loginRequest.email() .isBlank())
                ||
                (loginRequest.username() != null && !loginRequest.username().isBlank());

    }
}
