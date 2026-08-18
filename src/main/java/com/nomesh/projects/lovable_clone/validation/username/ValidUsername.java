package com.nomesh.projects.lovable_clone.validation.username;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.*;

@Documented
@Pattern(regexp = "^\\S*$", message = "Username must not contain spaces")
@Constraint(validatedBy = {})
@Retention(RetentionPolicy.RUNTIME)
@Target({
        ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT
})
public @interface ValidUsername {
    String message() default "Invalid username";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
