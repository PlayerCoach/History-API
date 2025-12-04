# Zadanie 5: Walidacja elementów (Bean Validation)

## Wymagania
- Użycie gotowych adnotacji Bean Validation do walidacji obiektu
- Implementacja własnego walidatora dla jednego z pól
- Wyświetlanie błędów walidacji na stronie edycji

**Punktacja:** 0.5 + 0.5 pkt

---

## Czym jest Bean Validation?

Bean Validation (Jakarta Validation) to specyfikacja Java EE/Jakarta EE do walidacji danych. Pozwala deklaratywnie (za pomocą adnotacji) określić reguły walidacji dla pól klas.

---

## Implementacja

### 1. Adnotacje walidacyjne na encji Note

**Plik:** `Note.java`

```java
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "notes")
public class Note implements Serializable {

    @Id
    private UUID id;

    // Tytuł: nie może być pusty, 3-100 znaków, bez wulgaryzmów
    @NotBlank(message = "{validation.note.title.notBlank}")
    @Size(min = 3, max = 100, message = "{validation.note.title.size}")
    @NoProfanity(message = "{validation.note.title.noProfanity}")
    @Column(nullable = false)
    private String title;

    // Treść: nie może być pusta, 10-5000 znaków
    @NotBlank(message = "{validation.note.content.notBlank}")
    @Size(min = 10, max = 5000, message = "{validation.note.content.size}")
    @Column(nullable = false)
    private String content;

    // Tryb: wymagany
    @NotNull(message = "{validation.note.mode.notNull}")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Mode mode;

    // ... pozostałe pola
}
```

---

### 2. Standardowe adnotacje Bean Validation

| Adnotacja | Opis | Przykład |
|-----------|------|----------|
| `@NotNull` | Pole nie może być null | `@NotNull private Mode mode;` |
| `@NotBlank` | String nie może być null, pusty lub same spacje | `@NotBlank private String title;` |
| `@NotEmpty` | Kolekcja/String nie może być null ani pusta | `@NotEmpty private List<Item> items;` |
| `@Size` | Rozmiar stringa/kolekcji w zakresie | `@Size(min=3, max=100)` |
| `@Min` | Minimalna wartość liczbowa | `@Min(0) private int age;` |
| `@Max` | Maksymalna wartość liczbowa | `@Max(100) private int percentage;` |
| `@Email` | Poprawny format email | `@Email private String email;` |
| `@Pattern` | Dopasowanie do wyrażenia regularnego | `@Pattern(regexp="[A-Z]{3}")` |
| `@Past` | Data w przeszłości | `@Past private LocalDate birthDate;` |
| `@Future` | Data w przyszłości | `@Future private LocalDate deadline;` |
| `@Positive` | Liczba > 0 | `@Positive private int quantity;` |
| `@PositiveOrZero` | Liczba >= 0 | `@PositiveOrZero private int count;` |
| `@Negative` | Liczba < 0 | `@Negative private int debt;` |
| `@DecimalMin` | Min wartość decimal | `@DecimalMin("0.01")` |
| `@DecimalMax` | Max wartość decimal | `@DecimalMax("999.99")` |
| `@Digits` | Liczba cyfr całkowitych i po przecinku | `@Digits(integer=5, fraction=2)` |

---

### 3. Własny walidator - @NoProfanity

#### 3.1. Definicja adnotacji

**Plik:** `NoProfanity.java`

```java
package pl.edu.pg.eti.kask.historyapi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Własny walidator sprawdzający czy tekst nie zawiera niedozwolonych słów.
 */
@Documented
@Constraint(validatedBy = NoProfanityValidator.class)  // ← Klasa implementująca logikę
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoProfanity {
    
    // Domyślna wiadomość błędu (klucz z pliku properties)
    String message() default "{validation.noProfanity}";
    
    // Grupy walidacji (wymagane przez specyfikację)
    Class<?>[] groups() default {};
    
    // Payload do przekazania dodatkowych metadanych (wymagane przez specyfikację)
    Class<? extends Payload>[] payload() default {};
}
```

#### 3.2. Implementacja walidatora

**Plik:** `NoProfanityValidator.java`

```java
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
        // Opcjonalna inicjalizacja - np. wczytanie listy słów z konfiguracji
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null/puste wartości są OK - obsługuje je @NotBlank
        if (value == null || value.isBlank()) {
            return true;
        }

        String lowerValue = value.toLowerCase();
        for (String forbidden : FORBIDDEN_WORDS) {
            if (lowerValue.contains(forbidden)) {
                return false;  // ← Znaleziono zakazane słowo
            }
        }
        return true;  // ← Wartość jest poprawna
    }
}
```

---

### 4. Plik z wiadomościami błędów

**Plik:** `ValidationMessages.properties`

```properties
# Bean Validation messages - Polish (Unicode escaped dla polskich znaków)
validation.note.title.notBlank=Tytu\u0142 nie mo\u017ce by\u0107 pusty
validation.note.title.size=Tytu\u0142 musi mie\u0107 od 3 do 100 znak\u00f3w
validation.note.title.noProfanity=Tytu\u0142 zawiera niedozwolone s\u0142owa
validation.note.content.notBlank=Tre\u015b\u0107 nie mo\u017ce by\u0107 pusta
validation.note.content.size=Tre\u015b\u0107 musi mie\u0107 od 10 do 5000 znak\u00f3w
validation.note.mode.notNull=Tryb jest wymagany
```

**Uwaga:** Polskie znaki muszą być escaped (np. `ł` → `\u0142`), ponieważ plik `ValidationMessages.properties` domyślnie używa kodowania ISO-8859-1.

---

### 5. Wyświetlanie błędów na stronie edycji

**Plik:** `note_edit.xhtml`

```xhtml
<h:form id="noteForm">
    
    <!-- Pole tytułu z walidacją -->
    <div class="mb-3">
        <label class="form-label" for="title">#{msg['notes.edit.field.title']}</label>
        <h:inputText 
            id="title" 
            value="#{noteEditView.note.title}" 
            styleClass="form-control"
            validatorMessage="#{msg['validation.note.title.size']}"
        />
        <!-- Wyświetlenie błędu walidacji dla tego pola -->
        <h:message for="title" styleClass="text-danger" />
    </div>

    <!-- Pole treści z walidacją -->
    <div class="mb-3">
        <label class="form-label" for="content">#{msg['notes.edit.field.content']}</label>
        <h:inputTextarea 
            id="content" 
            value="#{noteEditView.note.content}" 
            styleClass="form-control" 
            rows="5"
        />
        <h:message for="content" styleClass="text-danger" />
    </div>

    <!-- Pole trybu z walidacją -->
    <div class="mb-3">
        <label class="form-label" for="mode">#{msg['notes.edit.field.mode']}</label>
        <h:selectOneMenu id="mode" value="#{noteEditView.note.mode}" styleClass="form-select">
            <f:selectItem itemLabel="-- #{msg['notes.filter.allModes']} --" noSelectionOption="true" />
            <f:selectItems value="#{noteEditView.modes}" var="m" itemLabel="#{m.name()}" itemValue="#{m}" />
        </h:selectOneMenu>
        <h:message for="mode" styleClass="text-danger" />
    </div>

    <!-- Przycisk zapisu -->
    <h:commandButton value="#{msg['notes.action.save']}" action="#{noteEditView.save}" styleClass="btn btn-primary" />
    
</h:form>

<!-- Globalne wiadomości (np. dla OptimisticLockException) -->
<h:messages globalOnly="true" styleClass="alert alert-danger" />
```

---

### 6. Komponenty JSF do wyświetlania błędów

| Komponent | Opis |
|-----------|------|
| `<h:message for="fieldId"/>` | Błąd dla konkretnego pola |
| `<h:messages/>` | Wszystkie błędy na stronie |
| `<h:messages globalOnly="true"/>` | Tylko globalne błędy (bez powiązania z polem) |

---

## Przepływ walidacji

```
┌─────────────────────────────────────────────────────────────────┐
│                    PRZEPŁYW WALIDACJI                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  1. Użytkownik wypełnia formularz i klika "Zapisz"             │
│                                                                 │
│  2. JSF faza PROCESS_VALIDATIONS:                              │
│     ├── Konwersja wartości z formularza                        │
│     └── Wywołanie walidatorów Bean Validation                  │
│                                                                 │
│  3. Dla każdego pola z adnotacjami walidacyjnymi:              │
│     ├── @NotBlank → czy nie pusty?                             │
│     ├── @Size → czy w zakresie?                                │
│     └── @NoProfanity → czy bez zakazanych słów?                │
│                                                                 │
│  4. Jeśli walidacja NIEUDANA:                                  │
│     ├── Dodanie komunikatu błędu do FacesContext              │
│     ├── Przerwanie cyklu JSF                                   │
│     └── Renderowanie strony z błędami                          │
│                                                                 │
│  5. Jeśli walidacja UDANA:                                     │
│     └── Przejście do fazy UPDATE_MODEL_VALUES                  │
│         → Wywołanie akcji (np. noteEditView.save())           │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## Tworzenie własnego walidatora - checklist

1. ✅ **Stwórz adnotację** z `@Constraint(validatedBy = ...)`
2. ✅ **Dodaj wymagane elementy:** `message()`, `groups()`, `payload()`
3. ✅ **Stwórz klasę walidatora** implementującą `ConstraintValidator<A, T>`
4. ✅ **Zaimplementuj metody:** `initialize()` i `isValid()`
5. ✅ **Dodaj wiadomość błędu** do `ValidationMessages.properties`
6. ✅ **Użyj adnotacji** na polu encji

---

## Przykłady innych własnych walidatorów

### Walidator numeru telefonu

```java
@Constraint(validatedBy = PhoneNumberValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhoneNumber {
    String message() default "Invalid phone number";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{9,15}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return PHONE_PATTERN.matcher(value).matches();
    }
}
```

### Walidator porównujący dwa pola (hasło i potwierdzenie)

```java
@Constraint(validatedBy = PasswordMatchValidator.class)
@Target({ElementType.TYPE})  // Na poziomie klasy!
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordMatch {
    String message() default "Passwords do not match";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, UserForm> {
    @Override
    public boolean isValid(UserForm form, ConstraintValidatorContext context) {
        if (form.getPassword() == null) return true;
        return form.getPassword().equals(form.getConfirmPassword());
    }
}

// Użycie:
@PasswordMatch
public class UserForm {
    private String password;
    private String confirmPassword;
}
```

---

## Podsumowanie

✅ **Zrealizowane:**
- Adnotacje `@NotBlank`, `@Size`, `@NotNull` na encji `Note`
- Własny walidator `@NoProfanity` z implementacją `NoProfanityValidator`
- Plik `ValidationMessages.properties` z polskimi komunikatami błędów
- Wyświetlanie błędów walidacji na stronie edycji za pomocą `<h:message/>`
- Automatyczna walidacja podczas zapisu formularza

