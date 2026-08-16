package com.nomesh.projects.lovable_clone.validation.password;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private int min;
    private int max;

    @Override
    public void initialize(ValidPassword annotation) {
        this.min = annotation.min();
        this.max = annotation.max();
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isBlank())
            return true;
        if (password.length() < min || password.length() > max)
            return fail(context, "Password must be between " + min + " and " + max + " characters");
        return true;
    }

    private boolean fail(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        return false;
    }
}
