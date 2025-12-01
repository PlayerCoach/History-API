package pl.edu.pg.eti.kask.historyapi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Własny walidator sprawdzający czy tekst nie zawiera niedozwolonych słów.
 */
@Documented
@Constraint(validatedBy = NoProfanityValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoProfanity {
    
    String message() default "{validation.noProfanity}";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
