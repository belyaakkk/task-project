package com.belyak.taskproject.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Pattern(regexp = "^#?([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Color must be a valid HEX code (e.g. #FF0000)")
@Documented
public @interface HexColor {

    String message() default "Invalid HEX color format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}