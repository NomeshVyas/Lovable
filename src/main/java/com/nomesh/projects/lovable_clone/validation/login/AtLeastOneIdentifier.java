package com.nomesh.projects.lovable_clone.validation.login;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AtLeastOneIdentifierValidator.class)
public @interface AtLeastOneIdentifier {

    String message() default "Username or email must be provided";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
