# Zadanie 4: Filtrowanie listy elementów

## Wymagania
- Filtrowanie listy elementów po wszystkich polach
- Łączenie warunków operatorem AND
- Wszystkie wartości są opcjonalne
- Brak wprowadzonej wartości = nie brana pod uwagę
- Zapytanie budowane dynamicznie za pomocą Criteria API

**Punktacja:** 1 + 1 pkt

---

## Implementacja

### 1. Widok - formularz filtrowania

**Plik:** `notes.xhtml`

```xhtml
<!-- Formularz filtrowania -->
<h:form id="filterForm" styleClass="card mb-4">
    <div class="card-body">
        <h5 class="card-title">#{msg['notes.filter.title']}</h5>
        <div class="row g-3">
            
            <!-- Filtr po tytule -->
            <div class="col-md-3">
                <label class="form-label">#{msg['notes.column.title']}</label>
                <h:inputText
                    value="#{noteListView.filterTitle}"
                    styleClass="form-control"
                />
            </div>
            
            <!-- Filtr po treści -->
            <div class="col-md-3">
                <label class="form-label">#{msg['notes.edit.field.content']}</label>
                <h:inputText
                    value="#{noteListView.filterContent}"
                    styleClass="form-control"
                />
            </div>
            
            <!-- Filtr po trybie -->
            <div class="col-md-3">
                <label class="form-label">#{msg['notes.edit.field.mode']}</label>
                <h:selectOneMenu
                    value="#{noteListView.filterMode}"
                    styleClass="form-select"
                >
                    <f:selectItem
                        itemLabel="#{msg['notes.filter.allModes']}"
                        noSelectionOption="true"
                    />
                    <f:selectItems
                        value="#{noteListView.modes}"
                        var="m"
                        itemLabel="#{m.name()}"
                        itemValue="#{m}"
                    />
                </h:selectOneMenu>
            </div>
            
            <!-- Przyciski akcji -->
            <div class="col-md-3 d-flex align-items-end gap-2">
                <h:commandButton
                    value="#{msg['notes.filter.apply']}"
                    action="#{noteListView.filter}"
                    styleClass="btn btn-primary"
                />
                <h:commandButton
                    value="#{msg['notes.filter.clear']}"
                    action="#{noteListView.clearFilters}"
                    styleClass="btn btn-secondary"
                />
            </div>
        </div>
    </div>
</h:form>
```

---

### 2. Backing Bean - NoteListView

**Plik:** `NoteListView.java`

```java
@Named
@ViewScoped
public class NoteListView implements Serializable {

    @Inject
    private NoteService noteService;
    
    @Inject
    private SecurityContext securityContext;

    // Lista wyświetlanych notatek
    @Getter
    private List<Note> notes;

    // Pola filtrów
    @Getter @Setter
    private String filterTitle;

    @Getter @Setter
    private String filterContent;

    @Getter @Setter
    private Mode filterMode;

    @PostConstruct
    public void init() {
        // Na początku ładujemy wszystkie notatki (bez filtrów)
        loadNotes();
    }

    /**
     * Ładuje notatki z uwzględnieniem aktualnych filtrów.
     * Jeśli filtry są puste - zwraca wszystkie notatki.
     */
    private void loadNotes() {
        String ownerLogin = null;
        
        // Admin widzi wszystko, zwykły użytkownik tylko swoje
        if (!isAdmin()) {
            ownerLogin = getUsername();
        }

        // Wywołanie z filtrami (lub null jeśli puste)
        notes = noteService.findWithFilters(
            filterTitle,
            filterContent,
            filterMode,
            ownerLogin
        );
    }

    /**
     * Akcja przycisku "Filtruj"
     */
    public String filter() {
        loadNotes();
        return null; // Pozostań na tej samej stronie
    }

    /**
     * Akcja przycisku "Wyczyść"
     */
    public String clearFilters() {
        filterTitle = null;
        filterContent = null;
        filterMode = null;
        loadNotes();
        return null;
    }

    /**
     * Zwraca dostępne tryby dla selecta
     */
    public Mode[] getModes() {
        return Mode.values();
    }

    private boolean isAdmin() {
        return securityContext.isCallerInRole("admin");
    }

    private String getUsername() {
        return securityContext.getCallerPrincipal().getName();
    }
}
```

---

### 3. Warstwa serwisowa - NoteService

**Plik:** `NoteService.java`

```java
@Stateless
public class NoteService {

    @Inject
    private NoteRepository repository;

    /**
     * Znajduje notatki z dynamicznymi filtrami.
     * Wszystkie parametry są opcjonalne.
     */
    public List<Note> findWithFilters(String title, String content, Mode mode, String ownerLogin) {
        return repository.findWithFilters(title, content, mode, ownerLogin);
    }

    // ... pozostałe metody
}
```

---

### 4. Warstwa repozytorium - dynamiczne zapytanie Criteria API

**Plik:** `NoteRepository.java`

```java
@Stateless
public class NoteRepository {

    @PersistenceContext(unitName = "historyPU")
    private EntityManager em;

    /**
     * Dynamiczne filtrowanie notatek za pomocą Criteria API.
     * Wszystkie parametry są opcjonalne i łączone operatorem AND.
     * 
     * @param title      tytuł (zaczyna się od...)
     * @param content    treść (zaczyna się od...)
     * @param mode       tryb notatki
     * @param ownerLogin login właściciela
     * @return przefiltrowana lista notatek
     */
    public List<Note> findWithFilters(String title, String content, Mode mode, String ownerLogin) {
        // 1. Inicjalizacja Criteria API
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Note> cq = cb.createQuery(Note.class);
        Root<Note> root = cq.from(Note.class);

        // 2. Lista predykatów (warunków WHERE)
        List<Predicate> predicates = new ArrayList<>();

        // 3. Dodawanie warunków tylko dla niepustych parametrów
        
        // Filtr po tytule (case-insensitive, zaczyna się od)
        if (title != null && !title.isBlank()) {
            predicates.add(
                cb.like(cb.lower(root.get("title")), title.toLowerCase() + "%")
            );
        }

        // Filtr po treści (case-insensitive, zaczyna się od)
        if (content != null && !content.isBlank()) {
            predicates.add(
                cb.like(cb.lower(root.get("content")), content.toLowerCase() + "%")
            );
        }

        // Filtr po trybie (dokładne dopasowanie)
        if (mode != null) {
            predicates.add(
                cb.equal(root.get("mode"), mode)
            );
        }

        // Filtr po właścicielu (dla zwykłych użytkowników)
        if (ownerLogin != null && !ownerLogin.isBlank()) {
            predicates.add(
                cb.equal(root.get("createdBy").get("login"), ownerLogin)
            );
        }

        // 4. Złączenie warunków operatorem AND
        if (!predicates.isEmpty()) {
            cq.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        // 5. Wykonanie zapytania
        cq.select(root);
        return em.createQuery(cq).getResultList();
    }
}
```

---

## Jak działa dynamiczne filtrowanie?

### Scenariusz 1: Użytkownik nie wprowadził żadnego filtra

```java
title = null
content = null
mode = null
ownerLogin = "jan"  // (bo nie jest adminem)

predicates = [
    cb.equal(root.get("createdBy").get("login"), "jan")
]

// Wygenerowane SQL:
SELECT * FROM notes WHERE created_by_id = (SELECT id FROM users WHERE login = 'jan')
```

### Scenariusz 2: Użytkownik wprowadził tytuł i tryb

```java
title = "Nap"
content = null
mode = Mode.PRIVATE
ownerLogin = "jan"

predicates = [
    cb.like(cb.lower(root.get("title")), "nap%"),
    cb.equal(root.get("mode"), Mode.PRIVATE),
    cb.equal(root.get("createdBy").get("login"), "jan")
]

// Wygenerowane SQL:
SELECT * FROM notes 
WHERE LOWER(title) LIKE 'nap%' 
  AND mode = 'PRIVATE' 
  AND created_by_id = (SELECT id FROM users WHERE login = 'jan')
```

### Scenariusz 3: Admin bez filtrów

```java
title = null
content = null
mode = null
ownerLogin = null  // (bo jest adminem)

predicates = []  // pusta lista!

// Wygenerowane SQL:
SELECT * FROM notes
```

---

## Bundle Messages

**messages_pl.properties:**
```properties
notes.filter.title=Filtrowanie
notes.filter.apply=Filtruj
notes.filter.clear=Wyczyść
notes.filter.allModes=-- Wszystkie tryby --
```

**messages_en.properties:**
```properties
notes.filter.title=Filtering
notes.filter.apply=Filter
notes.filter.clear=Clear
notes.filter.allModes=-- All modes --
```

---

## Diagram przepływu

```
┌─────────────────────────────────────────────────────────────────┐
│                    PRZEPŁYW FILTROWANIA                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Użytkownik                                                    │
│      │                                                         │
│      ├──→ Wprowadza filtry w formularzu                       │
│      │                                                         │
│      ├──→ Klika "Filtruj"                                     │
│      │                                                         │
│      ▼                                                         │
│  NoteListView.filter()                                         │
│      │                                                         │
│      ├──→ Sprawdza: czy admin?                                │
│      │    ├── TAK → ownerLogin = null                         │
│      │    └── NIE → ownerLogin = username                     │
│      │                                                         │
│      ├──→ Wywołuje: noteService.findWithFilters(...)          │
│      │                                                         │
│      ▼                                                         │
│  NoteRepository.findWithFilters()                              │
│      │                                                         │
│      ├──→ Tworzy listę predykatów                             │
│      │                                                         │
│      ├──→ Dla każdego NIEPUSTEGO parametru:                   │
│      │    └── Dodaj predykat do listy                         │
│      │                                                         │
│      ├──→ Złącz predykaty operatorem AND                      │
│      │                                                         │
│      ├──→ Wykonaj zapytanie                                   │
│      │                                                         │
│      ▼                                                         │
│  Zwraca przefiltrowaną listę → wyświetla w tabeli             │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## Obsługa różnych typów filtrów

| Typ pola | Metoda CriteriaBuilder | Przykład |
|----------|------------------------|----------|
| String (contains) | `like()` | `cb.like(root.get("title"), "%" + value + "%")` |
| String (starts with) | `like()` | `cb.like(root.get("title"), value + "%")` |
| String (exact) | `equal()` | `cb.equal(root.get("code"), value)` |
| Enum | `equal()` | `cb.equal(root.get("status"), Status.ACTIVE)` |
| Number (exact) | `equal()` | `cb.equal(root.get("age"), 25)` |
| Number (range) | `between()` | `cb.between(root.get("price"), min, max)` |
| Date (after) | `greaterThanOrEqualTo()` | `cb.greaterThanOrEqualTo(root.get("date"), startDate)` |
| Date (before) | `lessThanOrEqualTo()` | `cb.lessThanOrEqualTo(root.get("date"), endDate)` |
| Boolean | `equal()` | `cb.equal(root.get("active"), true)` |

---

## Podsumowanie

✅ **Zrealizowane:**
- Formularz filtrowania z polami: tytuł, treść, tryb
- Wszystkie pola opcjonalne
- Łączenie warunków operatorem AND
- Dynamiczne budowanie zapytania Criteria API
- Obsługa uprawnień (admin vs zwykły użytkownik)
- Przyciski "Filtruj" i "Wyczyść"
- Pełne tłumaczenie PL/EN

