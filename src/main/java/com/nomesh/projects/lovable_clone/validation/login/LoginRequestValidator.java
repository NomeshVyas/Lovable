package com.nomesh.projects.lovable_clone.validation.login;

import com.nomesh.projects.lovable_clone.dto.auth.LoginRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

public class LoginRequestValidator implements ConstraintValidator<ValidLoginRequest, LoginRequest> {

    @Override
    public boolean isValid(LoginRequest loginRequest, ConstraintValidatorContext context) {
        if (loginRequest == null) return true;
        return StringUtils.hasText(loginRequest.email()) ^ StringUtils.hasText(loginRequest.username());
    }
}
