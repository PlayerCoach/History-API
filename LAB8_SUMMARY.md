# Lab 8 - Podsumowanie implementacji

## Zadanie 1: Optimistic Locking (Blokowanie optymistyczne)

### Zmiany w encji `Note.java`

```java
@Version
private Long version;
```

- Dodano pole `version` z adnotacją `@Version` do encji `Note`
- JPA automatycznie zarządza wersją przy każdej aktualizacji
- Przy konflikcie rzucany jest `OptimisticLockException`

### Obsługa konfliktu w `NoteEditView.java`

- Dodano pola `currentDatabaseNote`, `userEnteredNote`, `versionConflict`
- Metoda `handleVersionConflict()` - zachowuje dane użytkownika i pobiera aktualne z bazy
- Metoda `acceptCurrentVersion()` - przyjmuje wersję z bazy
- Metoda `retryWithUserData()` - próbuje zapisać dane użytkownika z nową wersją
- Metoda `isOptimisticLockException()` - wykrywa wyjątek opakowany w `EJBException`

### UI konfliktu w `note_edit.xhtml`

- Sekcja pokazująca obie wersje (z bazy i użytkownika) przy konflikcie
- Przyciski: "Przyjmij aktualną" i "Ponów zapis moich zmian"

---

## Zadanie 2: Znaczniki czasu (Created/Updated timestamps)

### Zmiany w encji `Note.java`

```java
@Column(updatable = false)
private LocalDateTime createdAt;

private LocalDateTime updatedAt;

@PrePersist
public void prePersist() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
}

@PreUpdate
public void preUpdate() {
    updatedAt = LocalDateTime.now();
}
```

- `createdAt` - ustawiany raz przy tworzeniu (updatable = false)
- `updatedAt` - aktualizowany przy każdej modyfikacji
- Użycie callbacków JPA: `@PrePersist` i `@PreUpdate`

### Wyświetlanie w `notes.xhtml`

- Dodano kolumny: Wersja, Data utworzenia, Data modyfikacja
- Formatowanie daty: `yyyy-MM-dd HH:mm`

---

## Zadanie 3: Konwersja JPQL na Criteria API

### `NoteRepository.java` - wszystkie metody przekonwertowane:

- `findAll()` - pobiera wszystkie notatki
- `findById()` - używa `em.find()` (bez zmian)
- `findByFigureId()` - filtrowanie po ID postaci
- `findByFigureIdAndOwner()` - filtrowanie po ID postaci i właścicielu
- `findByOwner()` - filtrowanie po loginie właściciela
- `deleteNotesWithHistoricalFigureId()` - usuwanie przez `CriteriaDelete`

### `UserRepository.java` - przekonwertowane:

- `findAll()` - Criteria API
- `findByLogin()` - Criteria API z `getResultStream().findFirst()`

### `HistoricalFigureRepository.java` - przekonwertowane:

- `findAll()` - Criteria API

---

## Zadanie 4: Dynamiczne filtrowanie z Criteria API

### Nowa metoda w `NoteRepository.java`

```java
public List<Note> findWithFilters(String title, String content, Mode mode, String ownerLogin) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Note> cq = cb.createQuery(Note.class);
    Root<Note> root = cq.from(Note.class);

    List<Predicate> predicates = new ArrayList<>();

    if (title != null && !title.isBlank()) {
        // Filtrowanie: tytuł zaczyna się od podanej frazy (case-insensitive)
        predicates.add(cb.like(cb.lower(root.get("title")), title.toLowerCase() + "%"));
    }

    if (content != null && !content.isBlank()) {
        predicates.add(cb.like(cb.lower(root.get("content")), content.toLowerCase() + "%"));
    }

    if (mode != null) {
        predicates.add(cb.equal(root.get("mode"), mode));
    }

    if (ownerLogin != null && !ownerLogin.isBlank()) {
        predicates.add(cb.equal(root.get("createdBy").get("login"), ownerLogin));
    }

    if (!predicates.isEmpty()) {
        cq.where(cb.and(predicates.toArray(new Predicate[0])));
    }

    return em.createQuery(cq).getResultList();
}
```

### `NoteListView.java` - pola filtrów

```java
@Getter @Setter
private String filterTitle;

@Getter @Setter
private String filterContent;

@Getter @Setter
private Mode filterMode;
```

- Metoda `filter()` - aplikuje filtry
- Metoda `clearFilters()` - czyści filtry

### Formularz filtrowania w `notes.xhtml`

- Pola: Tytuł, Treść, Tryb (select z opcją "Wszystkie")
- Przyciski: Filtruj, Wyczyść
- Filtrowanie typu "starts-with" (np. wpisanie "O" pokazuje notatki z tytułem na "O")

---

## Zadanie 5: Bean Validation z własnym walidatorem

### Adnotacja `@NoProfanity` (`validation/NoProfanity.java`)

```java
@Documented
@Constraint(validatedBy = NoProfanityValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoProfanity {
    String message() default "{validation.noProfanity}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

### Walidator `NoProfanityValidator.java`

```java
public class NoProfanityValidator implements ConstraintValidator<NoProfanity, String> {

    private static final Set<String> FORBIDDEN_WORDS = Set.of(
        "spam", "test123", "xxx", "fake"
    );

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
```

### Adnotacje Bean Validation w encji `Note.java`

```java
@NotBlank(message = "{validation.note.title.notBlank}")
@Size(min = 3, max = 100, message = "{validation.note.title.size}")
@NoProfanity(message = "{validation.note.title.noProfanity}")
@Column(nullable = false)
private String title;

@NotBlank(message = "{validation.note.content.notBlank}")
@Size(min = 10, max = 5000, message = "{validation.note.content.size}")
@Column(nullable = false)
private String content;

@NotNull(message = "{validation.note.mode.notNull}")
@Enumerated(EnumType.STRING)
@Column(nullable = false)
private Mode mode;
```

### Integracja Bean Validation z JSF (`note_edit.xhtml`)

JSF automatycznie integruje się z Bean Validation. Używamy `<f:validateBean />` aby wymusić walidację:

```xml
<h:inputText id="title" value="#{noteEditView.note.title}" styleClass="form-control">
    <f:validateBean />
</h:inputText>

<h:inputTextarea id="content" value="#{noteEditView.note.content}" styleClass="form-control" rows="5">
    <f:validateBean />
</h:inputTextarea>

<h:selectOneMenu id="mode" value="#{noteEditView.note.mode}" styleClass="form-select">
    <f:validateBean />
    <f:selectItems value="#{noteEditView.modes}" var="m" itemLabel="#{m.name()}" itemValue="#{m}" />
</h:selectOneMenu>
```

### Wyświetlanie błędów walidacji

Błędy Bean Validation są wyświetlane w czytelnym alercie Bootstrap na górze formularza:

```xml
<h:panelGroup rendered="#{facesContext.messageList.size() > 0}">
  <div class="alert alert-danger mb-4" role="alert">
    <ul class="mb-0 list-unstyled">
      <ui:repeat value="#{facesContext.messageList}" var="m">
        <li>#{m.detail}</li>
      </ui:repeat>
    </ul>
  </div>
</h:panelGroup>
```

### Komunikaty Bean Validation (`ValidationMessages.properties`)

```properties
validation.note.title.notBlank=Tytuł nie może być pusty
validation.note.title.size=Tytuł musi mieć od 3 do 100 znaków
validation.note.title.noProfanity=Tytuł zawiera niedozwolone słowa
validation.note.content.notBlank=Treść nie może być pusta
validation.note.content.size=Treść musi mieć od 10 do 5000 znaków
validation.note.mode.notNull=Tryb jest wymagany
```

**Uwaga:** Plik `ValidationMessages.properties` używa Unicode escapes dla polskich znaków (np. `\u0142` dla ł).

### Konfiguracja serwera (`server.xml`)

```xml
<feature>beanValidation-3.0</feature>
```

- Dodano feature Bean Validation do Open Liberty

---

## Struktura plików

```
src/main/java/pl/edu/pg/eti/kask/historyapi/
├── note/
│   ├── entity/
│   │   └── Note.java                    # @Version, timestamps, Bean Validation annotations
│   ├── repository/
│   │   └── NoteRepository.java          # Criteria API, findWithFilters()
│   └── service/
│       └── NoteService.java             # findWithFilters() delegation
├── user/repository/
│   └── UserRepository.java              # Criteria API
├── historicalfigure/repository/
│   └── HistoricalFigureRepository.java  # Criteria API
├── validation/
│   ├── NoProfanity.java                 # Custom validation annotation
│   └── NoProfanityValidator.java        # Custom validator implementation
└── view/note/
    ├── NoteEditView.java                # Optimistic lock handling
    └── NoteListView.java                # Filtering fields

src/main/resources/
├── ValidationMessages.properties        # Bean Validation messages (Unicode escaped)
└── bundles/
    └── messages_pl.properties           # JSF i18n messages (Unicode escaped)

src/main/liberty/config/
└── server.xml                           # beanValidation-3.0 feature

src/main/webapp/note/
├── notes.xhtml                          # Filter form, version/date columns
└── note_edit.xhtml                      # f:validateBean, validation alerts
```
