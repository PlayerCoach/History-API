# Zadanie 3: Zapytania Criteria API

## Wymagania
- Wszystkie zapytania JPQL powinny być zamienione na zapytania Criteria API
- Dynamiczne budowanie zapytań bez użycia stringów SQL/JPQL

**Punktacja:** 0.5 + 0.5 pkt

---

## Czym jest Criteria API?

Criteria API to programistyczne (type-safe) API do budowania zapytań w JPA. Zamiast pisać zapytania jako stringi JPQL, budujemy je za pomocą obiektów Java.

### Porównanie JPQL vs Criteria API

**JPQL (String-based):**
```java
String jpql = "SELECT n FROM Note n WHERE n.title = :title";
TypedQuery<Note> query = em.createQuery(jpql, Note.class);
query.setParameter("title", "Test");
```

**Criteria API (Type-safe):**
```java
CriteriaBuilder cb = em.getCriteriaBuilder();
CriteriaQuery<Note> cq = cb.createQuery(Note.class);
Root<Note> root = cq.from(Note.class);
cq.select(root).where(cb.equal(root.get("title"), "Test"));
List<Note> result = em.createQuery(cq).getResultList();
```

---

## Implementacja

### 1. Podstawowe zapytanie - findAll()

**Plik:** `NoteRepository.java`

```java
public List<Note> findAll() {
    // 1. Pobierz CriteriaBuilder z EntityManager
    CriteriaBuilder cb = em.getCriteriaBuilder();
    
    // 2. Stwórz zapytanie zwracające Note
    CriteriaQuery<Note> cq = cb.createQuery(Note.class);
    
    // 3. Określ tabelę źródłową (FROM Note)
    Root<Note> root = cq.from(Note.class);
    
    // 4. Wybierz wszystkie rekordy (SELECT root)
    cq.select(root);
    
    // 5. Wykonaj zapytanie
    return em.createQuery(cq).getResultList();
}
```

**Odpowiednik JPQL:**
```sql
SELECT n FROM Note n
```

---

### 2. Zapytanie z warunkiem - findById()

```java
public Optional<Note> findById(UUID id) {
    return Optional.ofNullable(em.find(Note.class, id));
}
```

**Uwaga:** Dla `findById` najlepiej użyć `em.find()` - jest zoptymalizowane dla wyszukiwania po kluczu głównym.

---

### 3. Zapytanie z warunkiem na powiązanej encji - findByFigureId()

```java
public List<Note> findByFigureId(UUID figureId) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Note> cq = cb.createQuery(Note.class);
    Root<Note> root = cq.from(Note.class);
    
    // Warunek: historicalFigure.id = figureId
    cq.select(root).where(
        cb.equal(root.get("historicalFigure").get("id"), figureId)
    );
    
    return em.createQuery(cq).getResultList();
}
```

**Odpowiednik JPQL:**
```sql
SELECT n FROM Note n WHERE n.historicalFigure.id = :figureId
```

---

### 4. Zapytanie z wieloma warunkami (AND) - findByFigureIdAndOwner()

```java
public List<Note> findByFigureIdAndOwner(UUID figureId, String username) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Note> cq = cb.createQuery(Note.class);
    Root<Note> root = cq.from(Note.class);
    
    // Łączenie warunków operatorem AND
    cq.select(root).where(
        cb.and(
            cb.equal(root.get("historicalFigure").get("id"), figureId),
            cb.equal(root.get("createdBy").get("login"), username)
        )
    );
    
    return em.createQuery(cq).getResultList();
}
```

**Odpowiednik JPQL:**
```sql
SELECT n FROM Note n 
WHERE n.historicalFigure.id = :figureId 
  AND n.createdBy.login = :username
```

---

### 5. Zapytanie z warunkiem na zagnieżdżonym polu

```java
public List<Note> findByOwner(String username) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Note> cq = cb.createQuery(Note.class);
    Root<Note> root = cq.from(Note.class);
    
    // root.get("createdBy") → nawigacja do User
    // .get("login") → dostęp do pola login w User
    cq.select(root).where(
        cb.equal(root.get("createdBy").get("login"), username)
    );
    
    return em.createQuery(cq).getResultList();
}
```

---

## Elementy Criteria API

### CriteriaBuilder - główne metody

| Metoda | Opis | Przykład |
|--------|------|----------|
| `equal(x, y)` | Równość | `cb.equal(root.get("title"), "Test")` |
| `notEqual(x, y)` | Nierówność | `cb.notEqual(root.get("status"), "DELETED")` |
| `like(x, pattern)` | LIKE | `cb.like(root.get("title"), "%test%")` |
| `gt(x, y)` | Większe niż | `cb.gt(root.get("version"), 5)` |
| `lt(x, y)` | Mniejsze niż | `cb.lt(root.get("price"), 100)` |
| `ge(x, y)` | Większe lub równe | `cb.ge(root.get("age"), 18)` |
| `le(x, y)` | Mniejsze lub równe | `cb.le(root.get("count"), 10)` |
| `between(x, a, b)` | Zakres | `cb.between(root.get("price"), 10, 100)` |
| `isNull(x)` | NULL check | `cb.isNull(root.get("deletedAt"))` |
| `isNotNull(x)` | NOT NULL check | `cb.isNotNull(root.get("email"))` |
| `and(...)` | Koniunkcja | `cb.and(pred1, pred2)` |
| `or(...)` | Alternatywa | `cb.or(pred1, pred2)` |
| `not(x)` | Negacja | `cb.not(predicate)` |
| `lower(x)` | Lowercase | `cb.lower(root.get("email"))` |
| `upper(x)` | Uppercase | `cb.upper(root.get("code"))` |
| `count(x)` | Zliczanie | `cb.count(root)` |
| `sum(x)` | Suma | `cb.sum(root.get("amount"))` |
| `avg(x)` | Średnia | `cb.avg(root.get("rating"))` |

---

### Root<T> - nawigacja po encjach

```java
Root<Note> root = cq.from(Note.class);

// Dostęp do pola prostego
root.get("title")                          // → String

// Nawigacja do powiązanej encji
root.get("historicalFigure")               // → HistoricalFigure

// Nawigacja do pola w powiązanej encji
root.get("historicalFigure").get("name")   // → String

// Głęboka nawigacja
root.get("createdBy").get("profile").get("email")
```

---

### Join - złączenia tabel

```java
CriteriaBuilder cb = em.getCriteriaBuilder();
CriteriaQuery<Note> cq = cb.createQuery(Note.class);
Root<Note> root = cq.from(Note.class);

// INNER JOIN
Join<Note, HistoricalFigure> figureJoin = root.join("historicalFigure");

// LEFT JOIN
Join<Note, User> userJoin = root.join("createdBy", JoinType.LEFT);

// Warunek na złączonej tabeli
cq.where(cb.equal(figureJoin.get("name"), "Napoleon"));
```

---

## Dynamiczne budowanie zapytań

Główna zaleta Criteria API - dynamiczne składanie warunków:

```java
public List<Note> findWithFilters(String title, String content, Mode mode, String ownerLogin) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Note> cq = cb.createQuery(Note.class);
    Root<Note> root = cq.from(Note.class);

    // Lista predykatów (warunków)
    List<Predicate> predicates = new ArrayList<>();

    // Dodawaj warunki tylko jeśli parametr nie jest pusty
    if (title != null && !title.isBlank()) {
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

    // Złącz wszystkie warunki operatorem AND
    if (!predicates.isEmpty()) {
        cq.where(cb.and(predicates.toArray(new Predicate[0])));
    }

    cq.select(root);
    return em.createQuery(cq).getResultList();
}
```

**Dlaczego to lepsze niż JPQL?**

W JPQL musielibyśmy dynamicznie składać string:
```java
// ❌ Brzydkie i podatne na błędy
StringBuilder jpql = new StringBuilder("SELECT n FROM Note n WHERE 1=1");
if (title != null) {
    jpql.append(" AND n.title LIKE :title");
}
// ...
```

---

## Pełna implementacja NoteRepository

```java
@Stateless
public class NoteRepository {

    @PersistenceContext(unitName = "historyPU")
    private EntityManager em;

    public List<Note> findAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Note> cq = cb.createQuery(Note.class);
        Root<Note> root = cq.from(Note.class);
        cq.select(root);
        return em.createQuery(cq).getResultList();
    }

    public List<Note> findWithFilters(String title, String content, Mode mode, String ownerLogin) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Note> cq = cb.createQuery(Note.class);
        Root<Note> root = cq.from(Note.class);

        List<Predicate> predicates = new ArrayList<>();

        if (title != null && !title.isBlank()) {
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

        cq.select(root);
        return em.createQuery(cq).getResultList();
    }

    public Optional<Note> findById(UUID id) {
        return Optional.ofNullable(em.find(Note.class, id));
    }

    public List<Note> findByFigureId(UUID figureId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Note> cq = cb.createQuery(Note.class);
        Root<Note> root = cq.from(Note.class);
        cq.select(root).where(cb.equal(root.get("historicalFigure").get("id"), figureId));
        return em.createQuery(cq).getResultList();
    }

    public List<Note> findByFigureIdAndOwner(UUID figureId, String username) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Note> cq = cb.createQuery(Note.class);
        Root<Note> root = cq.from(Note.class);
        cq.select(root).where(
            cb.and(
                cb.equal(root.get("historicalFigure").get("id"), figureId),
                cb.equal(root.get("createdBy").get("login"), username)
            )
        );
        return em.createQuery(cq).getResultList();
    }

    public List<Note> findByOwner(String username) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Note> cq = cb.createQuery(Note.class);
        Root<Note> root = cq.from(Note.class);
        cq.select(root).where(cb.equal(root.get("createdBy").get("login"), username));
        return em.createQuery(cq).getResultList();
    }

    public void delete(UUID id) {
        Note note = em.find(Note.class, id);
        if (note != null) {
            em.remove(note);
        }
    }

    public void save(Note note) {
        if (note.getId() != null && em.find(Note.class, note.getId()) != null) {
            em.merge(note);
        } else {
            em.persist(note);
        }
    }
}
```

---

## Zalety Criteria API

| Zaleta | Opis |
|--------|------|
| **Type-safety** | Błędy wykrywane w czasie kompilacji |
| **Refactoring** | IDE może bezpiecznie zmieniać nazwy pól |
| **Dynamiczne zapytania** | Łatwe budowanie zapytań z opcjonalnymi warunkami |
| **Brak SQL Injection** | Parametry są automatycznie escaped |
| **IDE support** | Autouzupełnianie, podpowiedzi |

## Wady Criteria API

| Wada | Opis |
|------|------|
| **Verbose** | Więcej kodu niż JPQL |
| **Czytelność** | Trudniejsze do czytania dla prostych zapytań |
| **Krzywa uczenia** | Więcej do nauki niż JPQL |

---

## Podsumowanie

✅ **Zrealizowane:**
- Wszystkie metody w `NoteRepository` używają Criteria API
- Dynamiczne budowanie zapytań z filtrami
- Obsługa warunków na powiązanych encjach
- Brak zapytań JPQL w stylu stringowym

