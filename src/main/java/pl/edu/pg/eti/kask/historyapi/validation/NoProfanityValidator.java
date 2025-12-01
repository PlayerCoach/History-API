package pl.edu.pg.eti.kask.historyapi.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

/**
 * Implementacja walidatora sprawdzającego czy tekst nie zawiera niedozwolonych słów.
 */
public class NoProfanityValidator implements ConstraintValidator<NoProfanity, String> {

    // Lista zakazanych słów (przykładowa)
    private static final Set<String> FORBIDDEN_WORDS = Set.of(
        "spam", "test123", "xxx", "fake"
    );

    @Override
    public void initialize(NoProfanity constraintAnnotation) {
        // Brak dodatkowej inicjalizacji
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // null/puste wartości obsługuje @NotBlank
        }

        String lowerValue = value.toLowerCase();
        for (String forbidden : FORBIDDEN_WORDS) {
            if (lowerValue.contains(forbidden)) {
                return false;
            }
        }
        return true;
    }
}
