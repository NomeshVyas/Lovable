package com.nomesh.projects.lovable_clone.validation.password;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = PasswordValidator.class)
@Target({
        ElementType.FIELD,
        ElementType.PARAMETER,
        ElementType.RECORD_COMPONENT
})
@Retention(RetentionPolicy.RUNTIME)
@NotBlank(message = "Password is required")
public @interface ValidPassword {

    String message() default "Invalid Password";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    int min() default 8;
    int max() default 50;
}
