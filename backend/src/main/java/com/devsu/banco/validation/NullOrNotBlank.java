package com.devsu.banco.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

// para PATCH: el campo puede omitirse, pero si viene no puede estar vacío
@Documented
@Constraint(validatedBy = NullOrNotBlankValidator.class)
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
public @interface NullOrNotBlank {

    String message() default "No puede estar vacío";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
